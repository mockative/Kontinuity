import Combine

internal struct NativeStateFlowPublisher<Output, Failure: Error, Unit>: Publisher {
    let nativeStateFlow: NativeStateFlow<Output, Failure, Unit>
    
    func receive<S>(subscriber: S) where S : Subscriber, Failure == S.Failure, Output == S.Input {
        let subscription = NativeStateFlowSubscription(nativeStateFlow: nativeStateFlow, subscriber: subscriber)
        subscriber.receive(subscription: subscription)
    }
}

internal class NativeStateFlowSubscription<Output, Failure, Unit, S: Subscriber>: Subscription where S.Input == Output, S.Failure == Failure {
    private var nativeCancellable: NativeCancellable<Unit>? = nil
    private var subscriber: S?
    
    init(nativeStateFlow: NativeStateFlow<Output, Failure, Unit>, subscriber: S) {
        self.subscriber = subscriber
        self.nativeCancellable = nativeStateFlow(
            "subscribe",
            { value, unit in
                // This won't actually be called
                return unit
            },
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
