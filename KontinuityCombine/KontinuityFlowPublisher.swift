import Combine
import KontinuityCore

internal struct KontinuityFlowPublisher<Output, Failure: Error, Unit>: Publisher {
    let kontinuityFlow: KontinuityFlow<Output, Failure, Unit>
    
    func receive<S>(subscriber: S) where S : Subscriber, Failure == S.Failure, Output == S.Input {
        let subscription = KontinuityFlowSubscription(kontinuityFlow: kontinuityFlow, subscriber: subscriber)
        subscriber.receive(subscription: subscription)
    }
}

internal class KontinuityFlowSubscription<Output, Failure, Unit, S: Subscriber>: Subscription where S.Input == Output, S.Failure == Failure {
    private var kontinuityCancellable: KontinuityCancellable<Unit>? = nil
    private var subscriber: S?
    
    init(kontinuityFlow: KontinuityFlow<Output, Failure, Unit>, subscriber: S) {
        self.subscriber = subscriber
        self.kontinuityCancellable = kontinuityFlow(
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
        
        _ = kontinuityCancellable?()
        kontinuityCancellable = nil
    }
}
