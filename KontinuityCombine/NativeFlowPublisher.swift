import Combine
import KontinuityCore

internal struct NativeFlowPublisher<Output, Failure: Error, Unit>: Publisher {
    let nativeFlow: NativeFlow<Output, Failure, Unit>
    
    func receive<S>(subscriber: S) where S : Subscriber, Failure == S.Failure, Output == S.Input {
        let subscription = NativeFlowSubscription(nativeFlow: nativeFlow, subscriber: subscriber)
        subscriber.receive(subscription: subscription)
    }
}

internal class NativeFlowSubscription<Output, Failure, Unit, S: Subscriber>: Subscription where S.Input == Output, S.Failure == Failure {
    private var nativeCancellable: NativeCancellable<Unit>? = nil
    private var subscriber: S?
    
    init(nativeFlow: NativeFlow<Output, Failure, Unit>, subscriber: S) {
        self.subscriber = subscriber
        self.nativeCancellable = nativeFlow(
            { item, unit in
                _ = self.subscriber?.receive(item)
                return unit
            },
            { error, unit in
                if let error = error {
                    self.subscriber?.receive(completion: .failure(error))
                } else {
                    self.subscriber?.receive(completion: .finished)
                }
                
                return unit
            }
        )
    }
    
    func request(_ demand: Subscribers.Demand) {
        // Nothing
    }
    
    func cancel() {
        subscriber = nil
        
        _ = nativeCancellable?()
        nativeCancellable = nil
    }
}
