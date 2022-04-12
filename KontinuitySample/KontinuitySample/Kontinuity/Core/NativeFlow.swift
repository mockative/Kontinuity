public typealias NativeFlow<Output, Failure: Error, Unit> = (
    _ onItem: @escaping NativeCallback<Output, Unit>,
    _ onComplete: @escaping NativeCallback<Failure?, Unit>
) -> NativeCancellable<Unit>
