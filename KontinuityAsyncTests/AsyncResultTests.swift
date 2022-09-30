import XCTest
import KontinuityCore
import KontinuityAsync

class AsyncResultTests: XCTestCase {
    
    private class TestValue { }

    func testCancellableInvoked() async {
        var cancelCount = 0
        let kontinuitySuspend: KontinuitySuspend<String, Error, Void> = { _, errorCallback in
            return {
                cancelCount += 1
                errorCallback(CancellationError(), ())
            }
        }
        let handle = Task {
            await asyncResult(for: kontinuitySuspend)
        }
        XCTAssertEqual(cancelCount, 0, "Cancellable shouldn't be invoked yet")
        handle.cancel()
        let handleResult = await handle.result
        XCTAssertEqual(cancelCount, 1, "Cancellable should be invoked once")
        guard case let .success(result) = handleResult else {
            XCTFail("Task should complete without an error")
            return
        }
        guard case .failure(_) = result else {
            XCTFail("Function should fail with an error")
            return
        }
    }
    
    func testCompletionWithValue() async {
        let value = TestValue()
        let kontinuitySuspend: KontinuitySuspend<TestValue, NSError, Void> = { resultCallback, _ in
            resultCallback(value, ())
            return { }
        }
        let result = await asyncResult(for: kontinuitySuspend)
        guard case let .success(receivedValue) = result else {
            XCTFail("Function should return without an error")
            return
        }
        XCTAssertIdentical(receivedValue, value, "Received incorrect value")
    }
    
    func testCompletionWithError() async {
        let sendError = NSError(domain: "Test", code: 0)
        let kontinuitySuspend: KontinuitySuspend<TestValue, NSError, Void> = { _, errorCallback in
            errorCallback(sendError, ())
            return { }
        }
        let result = await asyncResult(for: kontinuitySuspend)
        guard case let .failure(error) = result else {
            XCTFail("Function should throw an error")
            return
        }
        XCTAssertEqual(error as NSError, sendError, "Received incorrect error")
    }
}
