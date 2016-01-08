package som.interpreter.nodes.specialized.whileloops;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

import som.interpreter.nodes.literals.BlockNode;
import som.vmobjects.SBlock;


public abstract class WhileWithStaticBlocksNode extends AbstractWhileNode {
  @Child protected BlockNode receiver;
  @Child protected BlockNode argument;

  private WhileWithStaticBlocksNode(final BlockNode receiver, final BlockNode argument,
      final SBlock rcvr, final SBlock arg, final boolean predicateBool) {
    super(rcvr, arg, predicateBool);
    this.receiver = receiver;
    this.argument = argument;
  }

  @Override
  public final Object executeGeneric(final VirtualFrame frame) {
    SBlock rcvr = receiver.executeSBlock(frame);
    SBlock arg = argument.executeSBlock(frame);
    return executeEvaluated(frame, rcvr, arg);
  }

  @Override
  protected final DynamicObject doWhileConditionally(final SBlock loopCondition,
      final SBlock loopBody) {
    return doWhileUnconditionally(loopCondition, loopBody);
  }

  public static final class WhileTrueStaticBlocksNode extends WhileWithStaticBlocksNode {
    public WhileTrueStaticBlocksNode(final BlockNode receiver,
        final BlockNode argument, final SBlock rcvr, final SBlock arg) {
      super(receiver, argument, rcvr, arg, true);
    }
  }

  public static final class WhileFalseStaticBlocksNode extends WhileWithStaticBlocksNode {
    public WhileFalseStaticBlocksNode(final BlockNode receiver,
        final BlockNode argument, final SBlock rcvr, final SBlock arg) {
      super(receiver, argument, rcvr, arg, false);
    }
  }
}
