package tech.aregall.testcontainers.zitadel.infrastructure.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import zitadel.admin.v1.Admin;
import zitadel.admin.v1.AdminServiceGrpc;

@Service
public class ZitadelGrpcClient {

    @GrpcClient("zitadel")
    private AdminServiceGrpc.AdminServiceBlockingStub adminServiceBlockingStub;

    public String getInstanceName() {
        return adminServiceBlockingStub.getMyInstance(Admin.GetMyInstanceRequest.newBuilder().build())
                .getInstance()
                .getName();
    }

}
