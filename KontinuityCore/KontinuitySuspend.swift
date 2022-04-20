public typealias KontinuitySuspend<Result, Failure: Error, Unit> = (
    _ onResult: @escaping KontinuityCallback<Result, Unit>,
    _ onError: @escaping KontinuityCallback<Failure, Unit>
) -> KontinuityCancellable<Unit>
