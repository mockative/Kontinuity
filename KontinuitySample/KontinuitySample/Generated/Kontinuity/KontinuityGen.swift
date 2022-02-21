import Combine
import Foundation
import shared

extension AuthenticationService {
  var isLoggingIn$: KroutonPublisher<Swift.Bool, Error> {
    KroutonPublisher { AuthenticationServiceKt.isLoggingIn(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }
  }
  var intFlow$: KroutonPublisher<Swift.Int, Error> {
    KroutonPublisher { AuthenticationServiceKt.intFlow(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }
  }
  var doubleFlow$: KroutonPublisher<Swift.Double, Error> {
    KroutonPublisher { AuthenticationServiceKt.doubleFlow(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }
  }
  func login$(request: AuthenticationRequest?) -> KroutonFuture<shared.AuthenticationResponse, Error> {
    KroutonFuture { AuthenticationServiceKt.login(receiver: self, request: request, onSuccess: $0, onFailure: $1) }
  }

  func foo$(args: [String]) -> KroutonFuture<Swift.Void, Error> {
    KroutonFuture { AuthenticationServiceKt.foo(receiver: self, args: args, onSuccess: $0, onFailure: $1) }
  }

  /**
   *
   *  Doc string
   *  - Parameter request: Param String
   */
  func getFlows$(request: AuthenticationRequest) -> KroutonPublisher<[shared.AuthenticationResponse], Error> {
    KroutonPublisher { AuthenticationServiceKt.getFlows(receiver: self, request: request, onElement: $0, onSuccess: $1, onFailure: $2) }
  }
}
import Combine
import Foundation
import shared

extension OtherService {
  var myFlow$: KroutonPublisher<Swift.Bool, Error> {
    KroutonPublisher { OtherServiceKt.myFlow(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }
  }}

extension KotlinThrowable: Error {}
