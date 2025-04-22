package tech.aregall.testcontainers.zitadel.infrastructure.grpc;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import zitadel.admin.v1.Admin;
import zitadel.admin.v1.AdminServiceGrpc;
import zitadel.instance.v1.InstanceOuterClass.InstanceDetail;

import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class ZitadelGrpcClient {

    private static final Logger log = getLogger(ZitadelGrpcClient.class);

    @GrpcClient("zitadel")
    private AdminServiceGrpc.AdminServiceBlockingStub adminServiceBlockingStub;

    private InstanceDetail getInstance() {
        return adminServiceBlockingStub.getMyInstance(Admin.GetMyInstanceRequest.newBuilder().build()).getInstance();
    }

    public String getInstanceName() {
        return getInstance().getName();
    }

    @EventListener(ApplicationReadyEvent.class)
    void onReady() {
        try {
            final var isHealthy = nonNull(adminServiceBlockingStub.healthz(Admin.HealthzRequest.newBuilder().build()));
            log.debug("Zitadel gRPC client is healthy: {}", isHealthy);
        } catch (StatusRuntimeException e) {
            log.error("Failed to connect to Zitadel gRPC server", e);
        }
    }

}
