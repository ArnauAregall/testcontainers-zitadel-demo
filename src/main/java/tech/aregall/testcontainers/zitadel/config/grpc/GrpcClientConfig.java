package tech.aregall.testcontainers.zitadel.config.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
class GrpcClientConfig {

    /**
     * Add the authorization header to all gRPC calls.
     * @param authorizationHeader the authorization header to add.
     * @return the gRPC client interceptor.
     */
    @GrpcGlobalClientInterceptor
    ClientInterceptor authHeaderInterceptor(@Value("${app.zitadel.authorization-header}") String authorizationHeader) {

        return new ClientInterceptor() {

            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

                return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
                    @Override
                    public void start(ClientCall.Listener<RespT> responseListener, Metadata headers) {
                        Metadata.Key<String> authKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
                        headers.put(authKey, authorizationHeader);
                        super.start(responseListener, headers);
                    }
                };
            }
        };
    }

}
