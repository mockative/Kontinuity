import Combine
import KontinuityCore

internal struct KontinuitySuspendPublisher<Output, Failure: Error, Unit>: Publisher {
    let kontinuitySuspend: KontinuitySuspend<Output, Failure, Unit>
    
    func receive<S>(subscriber: S) where S : Subscriber, Failure == S.Failure, Output == S.Input {
        let subscription = KontinuitySuspendSubscription(kontinuitySuspend: kontinuitySuspend, subscriber: subscriber)
        subscriber.receive(subscription: subscription)
    }
}

internal class KontinuitySuspendSubscription<Output, Failure, Unit, S: Subscriber>: Subscription where S.Input == Output, S.Failure == Failure {
    private var kontinuityCancellable: KontinuityCancellable<Unit>? = nil
    private var subscriber: S?
    
    init(kontinuitySuspend: KontinuitySuspend<Output, Failure, Unit>, subscriber: S) {
        self.subscriber = subscriber
        self.kontinuityCancellable = kontinuitySuspend(
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
        
        _ = kontinuityCancellable?()
        kontinuityCancellable = nil
    }
}
