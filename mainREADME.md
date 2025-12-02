# Branch Appointment System
## Brief
- Branch Appointment system is the system that allows customers to schedule appointments in a branch.
- The customers:
  - Register to the system
  - Schedule appointments
    - Create an appointment
      - The customer can select the slot(optional) they prefer if they want an appointment at the specific time.
    - Cancel an appointment
      - The customer can cancel appointment which includes a reason why the appointment was cancelled.
      - The cancellation can happen any time even if the branch has confirmed the appointment.
- Branch Admins:
  - Confirm appointment
    - An email is sent to the customer stating that the appointment has been confirmed.
  - Cancel
    - Admins can cancel appointments with with the reason that will be sent to the customer on the cancellation notification
- Notifications
  - Notifications are sent when:
    - The customer makes an appointment.
    - The admin confirms an appointment.
    - The admin cancels an appointment
- Appointment statuses:
  - `PENDING CONFIRMATION` - The status when the customer has made an appointment.
  - `CONFIRMED` - The status when the admin has confirmed the appointment.
  - `CANCELLED` - The status when admin or customer cancel the appointments.

## Architecture
 - The Branch Appointment System is developed using microservice architecture. The advantage of using this architecture is that:
   - It makes applications easier to scale and faster to develop
   - Microservices are smaller and independently deployable.
   - It's easier to manage bug fixes and future releases
 - The following kafka topic must be created in order for the whole appointment flow to work:
   - Topic: ``{env}-appointment-notifications``
   - The appointment system uses kafka for any notification that is sent to the customers about their appointments.
 - The appointment system consists of the 4 microservices. Each service manages it's own database.
   - Auth Service
     - This service is responsible for user management.
       - Registration of users
       - Sign in of using
       - Issuing access tokens
       - Password recovery
     - This service is deployed separately since it can also be replaced with any external identity provider like [KEYCLOACK](https://www.keycloak.org/)
     - [Auth Service README.md](https://github.com/Menelis/appointment_system-auth-server/blob/main/README.md)
   - Branch Service
     - This service is responsible for any information regarding a branch
     - Only admins can create or update branches
     - [Branch Service README.md](https://github.com/Menelis/appointment_system-branch-service/blob/main/README.md)
   - Appointment service
     - This service is responsible for managing appointments:
       - Scheduling of appointment
       - Managing(confirm and cancellation) appointments
     - [Appointment Service README.md](https://github.com/Menelis/appointment_system-appointment-service/blob/main/README.md)
   - Notification Service
     - This service is responsible for sending out notification via email.
     - It read notification events from [Kafka](https://kafka.apache.org/) that is sent by other services. For example:
       - Auth Service
         - Sends an email when the user register for email confirmation.
         - Send an email for password recovery
       - Appointment Service
         - Sends an email when the customer schedule an appointment.
         - Sends and email when an admin confirm/cancel an appointment.
     - [Notification Service README.md](https://github.com/Menelis/appointment_system-notification-service)
 - Communication between microservices
   - [gRPC](https://grpc.io/) - A widely used for communication between internal microservices due to it's high performance and it's polyglot nature.
     - It uses HTTP/2 for transport, which enables features like multiplexing for reduced latency, and Protocol Buffers (protobuf) for efficient, binary serialization instead of text-based formats like JSON.
   - Services that request data from other services are clients
   - Services that serves data to other service are servers
   - Two examples in Appointment System
     1. Client - Appointment Service - Appointment System require information about the user and branch when sending notifications to the customer
     2. Servers
        1. Auth Service - This service is a server to serve appointment service with customer details
        2. Branch Service - This service is a server to serve appointment service with branch details.
 - Other Components
   - API Gateway
     - A server that acts as intermediary, a single point of entry for clients(UI) communicating with backend services.
     - It performs routing for client to their required backend services without client needing to know each microservice url.
     - [API Gateway README.md](https://github.com/Menelis/appointment_system-gateway/blob/main/README.md)
   - UI
     - This is the Frond End for users(admin and customers) to schedule appointments.
     - It communicates with backend services via API Gateway
     - It makes request to Auth Service for access tokens
     - [UI README.md](https://github.com/Menelis/appointment_system-ui/blob/main/README.md)
   - Shared Library
     - A class library for common functionality between microservices.
     - Any logic that can be used by multiple microservice must be defined in this shared library. For example:
       - [Protobuf](https://protobuf.dev/) messages for communication between services reside on this shared library.
     - **If you are not using public images that are referenced on java service, this library must be pushed to artifacts repository in order for java services to include it in build when running workflows.**
## Diagram architecture
![Appointment System](https://github.com/Menelis/appointment_system-auth-server/blob/main/diagram_v1.png)