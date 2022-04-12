import Combine

internal struct NativeSuspendPublisher<Output, Failure: Error, Unit>: Publisher {
    let nativeSuspend: NativeSuspend<Output, Failure, Unit>
    
    func receive<S>(subscriber: S) where S : Subscriber, Failure == S.Failure, Output == S.Input {
        let subscription = NativeSuspendSubscription(nativeSuspend: nativeSuspend, subscriber: subscriber)
        subscriber.receive(subscription: subscription)
    }
}

internal class NativeSuspendSubscription<Output, Failure, Unit, S: Subscriber>: Subscription where S.Input == Output, S.Failure == Failure {
    private var nativeCancellable: NativeCancellable<Unit>? = nil
    private var subscriber: S?
    
    init(nativeSuspend: NativeSuspend<Output, Failure, Unit>, subscriber: S) {
        self.subscriber = subscriber
        self.nativeCancellable = nativeSuspend(
            { output, unit in
                if let subscriber = self.subscriber {
                    _ = subscriber.receive(output)
                    subscriber.receive(completion: .finished)
                }
                
                return unit
            },
            { error, unit in
                self.subscriber?.receive(completion: .failure(error))
                
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
