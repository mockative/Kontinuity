import Combine
import Foundation
import shared

extension AuthenticationService {
  var isLoggingIn$: KroutonPublisher<Swift.Bool, Error> {
    KroutonPublisher { AuthenticationServiceKt.isLoggingIn(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }
  }
  @available(macOS 12.0, iOS 15.0, tvOS 15.0, watchOS 8.0, *)
  var isLoggingInAsync: AsyncThrowingPublisher<KroutonPublisher<Swift.Bool, Error>> {
    KroutonPublisher { AuthenticationServiceKt.isLoggingIn(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }.values
  }
  var intFlow$: KroutonPublisher<Swift.Int, Error> {
    KroutonPublisher { AuthenticationServiceKt.intFlow(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }
  }
  @available(macOS 12.0, iOS 15.0, tvOS 15.0, watchOS 8.0, *)
  var intFlowAsync: AsyncThrowingPublisher<KroutonPublisher<Swift.Int, Error>> {
    KroutonPublisher { AuthenticationServiceKt.intFlow(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }.values
  }
  var doubleFlow$: KroutonPublisher<Swift.Double, Error> {
    KroutonPublisher { AuthenticationServiceKt.doubleFlow(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }
  }
  @available(macOS 12.0, iOS 15.0, tvOS 15.0, watchOS 8.0, *)
  var doubleFlowAsync: AsyncThrowingPublisher<KroutonPublisher<Swift.Double, Error>> {
    KroutonPublisher { AuthenticationServiceKt.doubleFlow(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }.values
  }
  func login$(request: AuthenticationRequest?) -> KroutonFuture<shared.AuthenticationResponse, Error> {
    KroutonFuture { AuthenticationServiceKt.login(receiver: self, request: request, onSuccess: $0, onFailure: $1) }
  }

  @available(macOS 12.0, iOS 15.0, tvOS 15.0, watchOS 8.0, *)
  func loginAsync(request: AuthenticationRequest?) async throws -> AuthenticationResponse {
    KroutonFuture { AuthenticationServiceKt.login(receiver: self, request: request, onSuccess: $0, onFailure: $1) }.value
  }

  func foo$(args: [String]) -> KroutonFuture<Swift.Void, Error> {
    KroutonFuture { AuthenticationServiceKt.foo(receiver: self, args: args, onSuccess: $0, onFailure: $1) }
  }

  @available(macOS 12.0, iOS 15.0, tvOS 15.0, watchOS 8.0, *)
  func fooAsync(args: [String]) async throws {
    KroutonFuture { AuthenticationServiceKt.foo(receiver: self, args: args, onSuccess: $0, onFailure: $1) }.value
  }

  /**
   *
   *  Doc string
   *  - Parameter request: Param String
   */
  func getFlows$(request: AuthenticationRequest) -> KroutonPublisher<[shared.AuthenticationResponse], Error> {
    KroutonPublisher { AuthenticationServiceKt.getFlows(receiver: self, request: request, onElement: $0, onSuccess: $1, onFailure: $2) }
  }

  func getFlowsAsync(request: AuthenticationRequest) -> AsyncThrowingPublisher<KroutonPublisher<[shared.AuthenticationResponse], Error>> {
    KroutonPublisher { AuthenticationServiceKt.getFlows(receiver: self, request: request, onElement: $0, onSuccess: $1, onFailure: $2) }.values
  }
}
import Combine
import Foundation
import shared

extension OtherService {
  var myFlow$: KroutonPublisher<Swift.Bool, Error> {
    KroutonPublisher { OtherServiceKt.myFlow(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }
  }
  @available(macOS 12.0, iOS 15.0, tvOS 15.0, watchOS 8.0, *)
  var myFlowAsync: AsyncThrowingPublisher<KroutonPublisher<Swift.Bool, Error>> {
    KroutonPublisher { OtherServiceKt.myFlow(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }.values
  }}

extension KotlinThrowable: Error {}
