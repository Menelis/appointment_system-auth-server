package co.appointment.service.grpc.server;

import co.appointment.grpc.AuthServiceGrpc;
import co.appointment.grpc.GetUserResponse;
import co.appointment.grpc.GetVerificationTokenResponse;
import co.appointment.grpc.ProcessVerificationTokenRequest;
import co.appointment.persistence.UserEntity;
import co.appointment.persistence.UserRepository;
import co.appointment.persistence.VerificationTokenEntity;
import co.appointment.persistence.VerificationTokenRepository;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuthGrcpService extends AuthServiceGrpc.AuthServiceImplBase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    public void processUserByEmail(final StringValue request, final StreamObserver<GetUserResponse> responseObserver) {
        UserEntity user = userRepository
                .findByEmail(request.getValue())
                .orElseThrow(
                        () -> new RuntimeException(String.format("User not found with email: %s", request.getValue())));
        responseObserver.onNext(getUserResponse(user));
        responseObserver.onCompleted();
    }

    @Override
    public void processUserById(final StringValue request, final StreamObserver<GetUserResponse> responseObserver) {
        UserEntity user = userRepository
                .findById(UUID.fromString(request.getValue()))
                .orElseThrow(
                        () -> new RuntimeException(String.format("User not found with id: %s", request.getValue())));
        responseObserver.onNext(getUserResponse(user));
        responseObserver.onCompleted();
    }

    @Override
    public void processVerificationTokenByEmail(
            final ProcessVerificationTokenRequest request,
            final StreamObserver<GetVerificationTokenResponse> responseObserver) {
        VerificationTokenEntity verificationToken = verificationTokenRepository
                .findByVerificationToken(request.getEmail(), request.getTokenType())
                .orElseThrow(
                        () -> new RuntimeException("No verification token found with email=[%s] and token type=[%s]"
                                .formatted(request.getEmail(), request.getTokenType())));

        if (log.isTraceEnabled()) {
            log.trace("VerificationToken=[{}]", verificationToken);
        }
        responseObserver.onNext(getVerificationTokenResponse(verificationToken));
        responseObserver.onCompleted();
    }

    GetVerificationTokenResponse getVerificationTokenResponse(final VerificationTokenEntity verificationToken) {
        return GetVerificationTokenResponse.newBuilder()
                .setId(verificationToken.getId().toString())
                .setToken(verificationToken.getToken())
                .setExpiryDate(verificationToken.getExpiryDate())
                .setTokenType(verificationToken.getTokenType())
                .build();
    }

    private GetUserResponse getUserResponse(final UserEntity user) {
        GetUserResponse.Builder userResponse = GetUserResponse.newBuilder()
                .setId(user.getId().toString())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setContactNo(user.getContactNo())
                .setEmail(user.getEmail())
                .setFullName(String.format("%s %s", user.getFirstName(), user.getLastName()));
        return userResponse.build();
    }
}
