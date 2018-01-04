package som.primitives.reflection;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;

import bd.nodes.WithContext;
import bd.primitives.Primitive;
import som.interpreter.nodes.nary.QuaternaryExpressionNode;
import som.vm.Universe;
import som.vmobjects.SClass;
import som.vmobjects.SInvokable;
import som.vmobjects.SSymbol;


@GenerateNodeFactory
@Primitive(className = "Object", primitive = "perform:withArguments:inSuperclass:")
public abstract class PerformWithArgumentsInSuperclassPrim extends QuaternaryExpressionNode
    implements WithContext<PerformWithArgumentsInSuperclassPrim, Universe> {
  @Child private IndirectCallNode call = Truffle.getRuntime().createIndirectCallNode();

  @CompilationFinal protected Universe universe;

  @Override
  public PerformWithArgumentsInSuperclassPrim initialize(final Universe universe) {
    assert this.universe == null && universe != null;
    this.universe = universe;
    return this;
  }

  @Specialization
  public final Object doSAbstractObject(final Object receiver, final SSymbol selector,
      final Object[] argArr, final DynamicObject clazz) {
    CompilerAsserts.neverPartOfCompilation(
        "PerformWithArgumentsInSuperclassPrim.doSAbstractObject()");
    SInvokable invokable = SClass.lookupInvokable(clazz, selector, universe);
    return call.call(invokable.getCallTarget(),
        mergeReceiverWithArguments(receiver, argArr));
  }

  // TODO: remove duplicated code, also in symbol dispatch, ideally removing by optimizing this
  // implementation...
  @ExplodeLoop
  private static Object[] mergeReceiverWithArguments(final Object receiver,
      final Object[] argsArray) {
    Object[] arguments = new Object[argsArray.length + 1];
    arguments[0] = receiver;
    for (int i = 0; i < argsArray.length; i++) {
      arguments[i + 1] = argsArray[i];
    }
    return arguments;
  }
}
