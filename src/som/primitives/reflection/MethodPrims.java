package som.primitives.reflection;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

import bd.primitives.Primitive;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.dispatch.InvokeOnCache;
import som.interpreter.nodes.nary.EagerlySpecializableNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.primitives.arrays.ToArgumentsArrayNode;
import som.primitives.arrays.ToArgumentsArrayNodeFactory;
import som.vm.NotYetImplementedException;
import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SArray;
import som.vmobjects.SInvokable;
import som.vmobjects.SSymbol;


public final class MethodPrims {

  @GenerateNodeFactory
  @Primitive(className = "Method", primitive = "signature")
  @Primitive(className = "Primitive", primitive = "signature")
  public abstract static class SignaturePrim extends UnaryExpressionNode {
    @Specialization
    public final SAbstractObject doSMethod(final SInvokable receiver) {
      return receiver.getSignature();
    }
  }

  @GenerateNodeFactory
  @Primitive(className = "Method", primitive = "holder")
  @Primitive(className = "Primitive", primitive = "holder")
  public abstract static class HolderPrim extends UnaryExpressionNode {
    @Specialization
    public final DynamicObject doSMethod(final SInvokable receiver) {
      return receiver.getHolder();
    }
  }

  @GenerateNodeFactory
  @Primitive(className = "Method", primitive = "invokeOn:with:",
      extraChild = ToArgumentsArrayNodeFactory.class)
  @Primitive(className = "Primitive", primitive = "invokeOn:with:",
      extraChild = ToArgumentsArrayNodeFactory.class)
  @NodeChildren({
      @NodeChild(value = "receiver", type = ExpressionNode.class),
      @NodeChild(value = "target", type = ExpressionNode.class),
      @NodeChild(value = "somArr", type = ExpressionNode.class),
      @NodeChild(value = "argArr", type = ToArgumentsArrayNode.class,
          executeWith = {"somArr", "target"})})
  @Primitive(selector = "invokeOn:with:", noWrapper = true,
      extraChild = ToArgumentsArrayNodeFactory.class)
  public abstract static class InvokeOnPrim extends EagerlySpecializableNode {
    @Child private InvokeOnCache callNode = InvokeOnCache.create();

    public abstract Object executeEvaluated(VirtualFrame frame, SInvokable receiver,
        Object target, SArray somArr);

    @Override
    public final Object doPreEvaluated(final VirtualFrame frame,
        final Object[] args) {
      return executeEvaluated(frame, (SInvokable) args[0], args[1], (SArray) args[2]);
    }

    @Specialization
    public final Object doInvoke(final VirtualFrame frame,
        final SInvokable receiver, final Object target, final SArray somArr,
        final Object[] argArr) {
      return callNode.executeDispatch(frame, receiver, argArr);
    }

    @Override
    public ExpressionNode wrapInEagerWrapper(final SSymbol selector,
        final ExpressionNode[] arguments, final Universe context) {
      throw new NotYetImplementedException();
    }
  }
}
