package trufflesom.interpreter.supernodes;

import com.oracle.truffle.api.frame.VirtualFrame;

import trufflesom.interpreter.nodes.AbstractMessageSendNode;
import trufflesom.interpreter.nodes.dispatch.AbstractDispatchNode;
import trufflesom.vmobjects.SSymbol;


public final class UnaryArgSendNode extends AbstractMessageSendNode {
  private final int                   argIdx;
  private final SSymbol               selector;
  @Child private AbstractDispatchNode dispatchNode;

  public UnaryArgSendNode(final int argIdx, final SSymbol selector,
      final AbstractDispatchNode dispatchNode) {
    super(1, null);
    this.argIdx = argIdx;
    this.selector = selector;
    this.dispatchNode = dispatchNode;
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    Object[] arguments = new Object[] {frame.getArguments()[argIdx]};
    return doPreEvaluated(frame, arguments);
  }

  @Override
  public Object doPreEvaluated(final VirtualFrame frame,
      final Object[] arguments) {
    return dispatchNode.executeDispatch(frame, arguments);
  }

  @Override
  public SSymbol getInvocationIdentifier() {
    return selector;
  }

  @Override
  public void notifyDispatchInserted() {
    dispatchNode.notifyAsInserted();
  }
}
