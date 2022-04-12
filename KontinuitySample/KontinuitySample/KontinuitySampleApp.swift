import SwiftUI
import shared

func foo(authenticationService: NativeAuthenticationService) async throws {
    createPublisher(for: authenticationService.doubleFlowNative)
        .sink { completion in
            // Nothing
        } receiveValue: { value in
            print("Value: \(value)")
        }
    
    let value = getValue(of: authenticationService.stateFlowNative)
    print("value: \(value)")
}

class NativeAuthenticationServiceMock : NativeAuthenticationServiceWrapper {
}

@main
struct KontinuitySampleApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
