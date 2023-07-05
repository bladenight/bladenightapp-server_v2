package app.bladenight.server.rpchandlers;

import app.bladenight.common.network.BladenightError;
import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.network.messages.FriendMessage;
import app.bladenight.common.network.messages.GpsInfo;
import app.bladenight.common.network.messages.RealTimeUpdateData;
import app.bladenight.common.procession.Participant;
import app.bladenight.common.procession.ParticipantInput;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.relationships.RelationshipMember;
import app.bladenight.common.relationships.RelationshipStore;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RpcHandlerGetRealtimeUpdate extends RpcHandler {

    private final Procession procession;
    private final boolean allowParticipation;
    private final Optional<RelationshipStore> relationshipStoreOptional;

    private Participant actualParticipant;

    public RpcHandlerGetRealtimeUpdate(Procession procession, Optional<RelationshipStore> relationshipStoreOptional, boolean allowParticipation) {
        this.procession = procession;
        this.relationshipStoreOptional = relationshipStoreOptional;
        this.allowParticipation = allowParticipation;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        GpsInfo gpsInput = rpcCall.getInput(GpsInfo.class);

        if (!validateInput(rpcCall, gpsInput))
            return;

        if (procession == null) {
            rpcCall.setError(BladenightError.INTERNAL_ERROR.getText(), "Internal error: Procession is null");
            return;
        }

        RealTimeUpdateData data = new RealTimeUpdateData();

        if (gpsInput != null) {
            boolean wantsToParticipate = gpsInput.isParticipating();
            boolean doesParticipate = wantsToParticipate && this.allowParticipation;
            if (wantsToParticipate && !doesParticipate) {
                getLog().warn("Participant with device id \"" + gpsInput.getDeviceId() + "\" would like to participate, but is not allowed to");
            }
            ParticipantInput participantInput = new ParticipantInput(gpsInput.getDeviceId(), doesParticipate, gpsInput.getLatitude(), gpsInput.getLongitude(), gpsInput.getAccuracy(), gpsInput.getRealUserSpeed());
            Participant participant = procession.updateParticipant(participantInput);
            actualParticipant = participant;
            data.isUserOnRoute(participant.isOnRoute());
            data.setUserPosition((long) participant.getLinearPosition(), (long) participant.getLinearSpeed(), gpsInput.getRealUserSpeed(), participant.getLatitude(), participant.getLongitude());
        }

        double routeLength = procession.getRoute().getLength();

        int participantsCount = Integer.MAX_VALUE;

        data.setHead(procession.getHead());
        data.getHead().setEstimatedTimeToArrival((long) (procession.evaluateTravelTimeBetween(data.getHead().getPosition(), routeLength)));

        if (data.getHead().getEstimatedTimeToArrival() < 0) {
            data.getHead().setEstimatedTimeToArrival(0); //when not running no estimation possible
        }
        data.setTail(procession.getTail());

        data.getTail().setEstimatedTimeToArrival((long) (procession.evaluateTravelTimeBetween(data.getTail().getPosition(), routeLength)));
        if (data.getTail().getEstimatedTimeToArrival() < 0) {
            data.getTail().setEstimatedTimeToArrival(0); //when not running no estimation possible
        }

        //TODO maximum time is eventTime and push all speed TIMES IN segments to MASTERApp
        data.setRouteLength((int) procession.getRoute().getLength());
        data.setRouteName(procession.getRoute().getName());
        data.setEventStatus(procession.getEventStatus());
        data.setUserTotal(procession.getParticipantCount());
        data.setUserOnRoute(procession.getParticipantsOnRoute());
        data.getUser().setEstimatedTimeToArrival((long) (procession.evaluateTravelTimeBetween(data.getUser().getPosition(), routeLength)));

        //Participants added to friendList
        ArrayList<Participant> alsoAddedParticipants = new ArrayList<Participant>();
        if (gpsInput != null && actualParticipant != null) {
            try {
                //Add participant when not in headlist if it has Head or Tail function set
                if (((gpsInput.getSpecialFunction() == 1) ||
                        (gpsInput.getSpecialFunction() == 5)) &&
                        data.isUserOnRoute() &&
                        !procession.trackedHeads.containsKey(actualParticipant.getDeviceId())) {
                    procession.trackedHeads.put(actualParticipant.getDeviceId(), actualParticipant);
                    getLog().trace(actualParticipant.getDeviceId() + " added in HeadList");

                } else if (procession.trackedHeads.containsKey(actualParticipant.getDeviceId())) {
                    procession.trackedHeads.remove(actualParticipant.getDeviceId());
                    getLog().trace(actualParticipant.getDeviceId() + " removed from HeadList");
                }
                if (((gpsInput.getSpecialFunction() == 2 || gpsInput.getSpecialFunction() == 6)) &&
                        !procession.trackedTails.containsKey(actualParticipant.getDeviceId())) {
                    procession.trackedTails.put(actualParticipant.getDeviceId(), actualParticipant);
                    getLog().trace(actualParticipant.getDeviceId() + " added in TailList");
                } else if (actualParticipant != null && procession.trackedTails.containsKey(actualParticipant.getDeviceId())) {
                    procession.trackedTails.remove(actualParticipant.getDeviceId());
                    getLog().trace(actualParticipant.getDeviceId() + " removed from TailList");
                }
            } catch (Exception e) {
                getLog().error("Head or Tail update failed", e);
            }
        }

        //Add Friends to list
        try {
            if (gpsInput != null && relationshipStoreOptional.isPresent()) {
                List<RelationshipMember> relationships = relationshipStoreOptional.get().getFinalizedRelationships(gpsInput.getDeviceId());
                for (RelationshipMember relationshipMember : relationships) {
                    Participant participant = procession.getParticipant(relationshipMember.getDeviceId());
                    FriendMessage friendMessage;
                    if (participant != null) {
                        friendMessage = new FriendMessage();
                        friendMessage.copyFrom(participant.getLastKnownPoint());
                        friendMessage.setEstimatedTimeToArrival((long) (procession.evaluateTravelTimeBetween(participant.getLinearPosition(), routeLength)));
                        data.addFriend(relationshipMember.getFriendId(), friendMessage);
                        alsoAddedParticipants.add(participant);
                    }
                }
            }
        } catch (Exception e) {
            getLog().error("RealTimeUpdateData execute failed", e);
        }

        //Here we go and add Head and Tail and other user in proccession to friendlist for EventManagers
        try {
            //add all participants when user has special function like Eventmanager
            if (gpsInput != null && gpsInput.getSpecialFunction() >= 4 && actualParticipant != null) {
                final List<Participant> participants = procession.getParticipants();
                int maxCount = 100;
                //create List of Participants
                for (Participant processionParticipant : participants
                ) {
                    maxCount--; //limit size
                    if (maxCount <= 0) break;
                    if (!processionParticipant.isOnRoute()) continue;
                    if (processionParticipant.equals(actualParticipant)) continue; //don't copy yourself
                    if (alsoAddedParticipants.contains(processionParticipant)) continue; // don't copy a friend again

                    //add all heads but not yourself
                    if (!procession.trackedHeads.isEmpty() && procession.trackedHeads.containsKey(processionParticipant.getDeviceId())) {
                        FriendMessage friendMessage = new FriendMessage();
                        friendMessage.copyFrom(processionParticipant.getLastKnownPoint());
                        friendMessage.setEstimatedTimeToArrival((long) (procession.evaluateTravelTimeBetween(processionParticipant.getLinearPosition(), routeLength)));
                        friendMessage.isOnline(true);
                        friendMessage.setFriendSpecialValue(1);
                        data.addFriend(participantsCount--, friendMessage);
                        continue;
                    }
                    //add all tails but not yourself
                    if (!procession.trackedTails.isEmpty() && procession.trackedTails.containsKey(processionParticipant.getDeviceId())) {
                        FriendMessage friendMessage = new FriendMessage();
                        friendMessage.copyFrom(processionParticipant.getLastKnownPoint());
                        friendMessage.setEstimatedTimeToArrival((long) (procession.evaluateTravelTimeBetween(processionParticipant.getLinearPosition(), routeLength)));
                        friendMessage.isOnline(true);
                        friendMessage.setFriendSpecialValue(2);
                        data.addFriend(participantsCount--, friendMessage);
                        continue;
                    }
                    FriendMessage friendMessage = new FriendMessage();
                    friendMessage.copyFrom(processionParticipant.getLastKnownPoint());
                    friendMessage.setEstimatedTimeToArrival((long) (procession.evaluateTravelTimeBetween(processionParticipant.getLinearPosition(), routeLength)));
                    friendMessage.isOnline(true);
                    friendMessage.setFriendSpecialValue(99); //value 99 is participant
                    data.addFriend(participantsCount--, friendMessage);
                }
            }
        } catch (Exception e) {
            getLog().error("RealTimeUpdateData all Participants execute failed", e);
        }
        rpcCall.setOutput(data, RealTimeUpdateData.class);
        alsoAddedParticipants.clear();
    }

    public boolean validateInput(RpcCall rpcCall, GpsInfo input) {
        if (input == null)
            return true;

        if (input.getDeviceId() == null || input.getDeviceId().length() == 0) {
            rpcCall.setError(BladenightUrl.BASE + "invalidInput", "Invalid input: " + input);
            return false;
        }
        return true;
    }


    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerGetRealtimeUpdate.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RpcHandlerGetRealtimeUpdate.class.getName());
        return log;
    }
}
