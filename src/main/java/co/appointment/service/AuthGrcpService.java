package co.appointment.service;

import co.appointment.entity.User;
import co.appointment.grpc.AuthServiceGrpc;
import co.appointment.grpc.GetUserByEmailRequest;
import co.appointment.grpc.GetUserByIdRequest;
import co.appointment.grpc.GetUserResponse;
import co.appointment.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

/**
 * A service to send user details to other microservies via intercommunication.
 * Nice blog on {@link } <a href="https://medium.com/@AlexanderObregon/getting-started-with-grpc-in-spring-boot-using-the-grpcservice-annotation-2decabbb3a02">...</a>
 */
@GrpcService
@RequiredArgsConstructor
public class AuthGrcpService extends AuthServiceGrpc.AuthServiceImplBase {

    private final UserRepository userRepository;

    @Override
    public void processUserByEmail(final GetUserByEmailRequest request, final StreamObserver<GetUserResponse> responseObserver) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException(String.format("User not found with email: %s", request.getEmail())));
        responseObserver.onNext(getUserResponse(user));
        responseObserver.onCompleted();
    }
    @Override
    public void processUserById(final GetUserByIdRequest request, final StreamObserver<GetUserResponse> responseObserver) {
        User user = userRepository.findById(UUID.fromString(request.getId())).orElseThrow(() -> new RuntimeException(String.format("User not found with id: %s", request.getId())));
        responseObserver.onNext(getUserResponse(user));
        responseObserver.onCompleted();
    }
    private GetUserResponse getUserResponse(final User user) {
        GetUserResponse.Builder userResponse = GetUserResponse.newBuilder()
                .setId(user.getId().toString())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setContactNo(user.getContactNo())
                .setEmail(user.getEmail())
                .setEmail(user.getEmail());

        //Add roles to userResponse
        user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .forEach(userResponse::addRoles);
        return userResponse.build();
    }
}
