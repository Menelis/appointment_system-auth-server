package co.appointment.service.grpc.server;

import co.appointment.grpc.AuthServiceGrpc;
import co.appointment.grpc.GetUserByEmailRequest;
import co.appointment.grpc.GetUserByIdRequest;
import co.appointment.grpc.GetUserResponse;
import co.appointment.persistence.UserEntity;
import co.appointment.persistence.UserRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class AuthGrcpService extends AuthServiceGrpc.AuthServiceImplBase {

    private final UserRepository userRepository;

    @Override
    public void processUserByEmail(final GetUserByEmailRequest request, final StreamObserver<GetUserResponse> responseObserver) {
        UserEntity user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException(String.format("User not found with email: %s", request.getEmail())));
        responseObserver.onNext(getUserResponse(user));
        responseObserver.onCompleted();
    }
    @Override
    public void processUserById(final GetUserByIdRequest request, final StreamObserver<GetUserResponse> responseObserver) {
        UserEntity user = userRepository.findById(UUID.fromString(request.getId())).orElseThrow(() -> new RuntimeException(String.format("User not found with id: %s", request.getId())));
        responseObserver.onNext(getUserResponse(user));
        responseObserver.onCompleted();
    }
    private GetUserResponse getUserResponse(final UserEntity user) {
        GetUserResponse.Builder userResponse = GetUserResponse.newBuilder()
                .setId(user.getId().toString())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setContactNo(user.getContactNo())
                .setEmail(user.getEmail())
                .setFullName(String.format("%s %s", user.getFirstName(), user.getLastName()));

        //Add roles to userResponse
//        user.getUserRoles().stream()
//                .map(ur -> ur.getRole().getName())
//                .forEach(userResponse::addRoles);
        return userResponse.build();
    }
}
