package som.primitives.arrays;

import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import bd.primitives.Primitive;
import bd.primitives.Specializer;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.nary.BinaryExpressionNode.BinarySystemOperation;
import som.vm.Universe;
import som.vmobjects.SArray;
import som.vmobjects.SSymbol;


@Primitive(className = "Array", primitive = "new:", selector = "new:", classSide = true,
    inParser = false, specializer = NewPrim.IsArrayClass.class)
public abstract class NewPrim extends BinarySystemOperation {

  public static class IsArrayClass extends Specializer<Universe, ExpressionNode, SSymbol> {
    public IsArrayClass(final Primitive prim, final NodeFactory<ExpressionNode> fact,
        final Universe universe) {
      super(prim, fact, universe);
    }

    @Override
    public boolean matches(final Object[] args, final ExpressionNode[] argNodes) {
      return args[0] == context.arrayClass;
    }
  }

  @Specialization(guards = "receiver == universe.arrayClass")
  public final SArray doSClass(final DynamicObject receiver, final long length) {
    return new SArray(length);
  }
}
