import Combine
import Foundation
import shared

extension AuthenticationService {
  var isLoggingIn: KroutonPublisher<Swift.Bool, Error> {
    KroutonPublisher { AuthenticationServiceKt_isLoggingIn(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }
  }}
