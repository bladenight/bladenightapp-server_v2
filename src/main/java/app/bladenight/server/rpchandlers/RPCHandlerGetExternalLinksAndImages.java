package app.bladenight.server.rpchandlers;

import app.bladenight.common.imagesandlinks.ImagesAndLinksList;
import app.bladenight.common.network.messages.ImagesAndLinksListMessage;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;

public class RPCHandlerGetExternalLinksAndImages extends RpcHandler {

    private final ImagesAndLinksList imagesAndLinksList;

    public RPCHandlerGetExternalLinksAndImages(ImagesAndLinksList imageAndLinkList) {
        this.imagesAndLinksList = imageAndLinkList;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        rpcCall.setOutput(ImagesAndLinksListMessage.newFromImagesAndLinks(imagesAndLinksList), ImagesAndLinksListMessage.class);
    }
}
