package org.apache.plc4x.java.can.api.conversation.canopen;

import io.vavr.control.Either;
import org.apache.plc4x.java.api.exceptions.PlcException;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.can.canopen.CANOpenFrame;
import org.apache.plc4x.java.canopen.readwrite.*;
import org.apache.plc4x.java.canopen.readwrite.io.DataItemIO;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public abstract class CANOpenConversationBase {

    protected final CANConversation<CANOpenFrame> delegate;
    protected final int nodeId;
    protected final int answerNodeId;

    public CANOpenConversationBase(CANConversation<CANOpenFrame> delegate, int nodeId, int answerNodeId) {
        this.delegate = delegate;
        this.nodeId = nodeId;
        this.answerNodeId = answerNodeId;
    }

    protected PlcValue decodeFrom(byte[] data, CANOpenDataType type, int length) throws ParseException {
        return DataItemIO.staticParse(new ReadBuffer(data, true), type, length);
    }

    protected <T> void onError(CompletableFuture<T> receiver, CANOpenSDOResponse response, Throwable error) {
        if (error != null) {
            receiver.completeExceptionally(error);
            return;
        }

        if (response.getResponse() instanceof SDOAbortResponse) {
            SDOAbortResponse abort = (SDOAbortResponse) response.getResponse();
            SDOAbort sdoAbort = abort.getAbort();
            receiver.completeExceptionally(new PlcException("Could not read value. Remote party reported code " + sdoAbort.getCode()));
        }
    }

    protected <X extends SDOResponse> Either<SDOAbort, X> unwrap(Class<X> payload, SDOResponse response) {
        if (response instanceof SDOAbortResponse) {
            return Either.left(((SDOAbortResponse) response).getAbort());
        }
        if (payload.isInstance(response)) {
            return Either.right((X) response);
        }
        throw new RuntimeException("Unexpected payload kind " + response);
    }

    protected CANOpenFrame createFrame(SDORequest rq) {
        return delegate.createBuilder()
            .withNodeId(nodeId)
            .withService(CANOpenService.RECEIVE_SDO)
            .withPayload(new CANOpenSDORequest(rq.getCommand(), rq))
            .build();
    }

    static class NodeIdPredicate implements Predicate<CANOpenFrame> {

        private final int nodeId;

        NodeIdPredicate(int nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public boolean test(CANOpenFrame frame) {
            return frame.getNodeId() == nodeId && frame.getService() == CANOpenService.TRANSMIT_SDO;
        }

        @Override
        public String toString() {
            return "NodeIdPredicate [" + nodeId + "]";
        }
    }

    static class TypePredicate<T, X> implements Predicate<X> {

        private final Class<T> type;

        public TypePredicate(Class<T> type) {
            this.type = type;
        }

        @Override
        public boolean test(X value) {
            return type.isInstance(value);
        }

        public String toString() {
            return "Type [" + type + "]";
        }
    }

    static class TypeOrAbortPredicate<T extends SDOResponse> extends TypePredicate<T, SDOResponse> {

        public TypeOrAbortPredicate(Class<T> type) {
            super(type);
        }

        @Override
        public boolean test(SDOResponse response) {
            return super.test(response) || response instanceof SDOAbortResponse;
        }
    }
}
