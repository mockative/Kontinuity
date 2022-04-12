public typealias NativeSuspend<Result, Failure: Error, Unit> = (
    _ onResult: @escaping NativeCallback<Result, Unit>,
    _ onError: @escaping NativeCallback<Failure, Unit>
) -> NativeCancellable<Unit>
