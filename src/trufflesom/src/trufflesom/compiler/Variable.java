package trufflesom.compiler;

import static trufflesom.compiler.bc.BytecodeGenerator.emitPOPARGUMENT;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPOPLOCAL;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPUSHARGUMENT;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPUSHLOCAL;
import static trufflesom.vm.SymbolTable.strBlockSelf;
import static trufflesom.vm.SymbolTable.strSelf;
import static trufflesom.vm.SymbolTable.symBlockSelf;
import static trufflesom.vm.SymbolTable.symSelf;
import static trufflesom.vm.SymbolTable.symbolFor;

import java.util.Objects;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.source.Source;

import trufflesom.bdt.source.SourceCoordinate;
import trufflesom.compiler.bc.BytecodeMethodGenContext;
import trufflesom.interpreter.nodes.ArgumentReadNode.LocalArgumentReadNode;
import trufflesom.interpreter.nodes.ArgumentReadNode.LocalArgumentWriteNode;
import trufflesom.interpreter.nodes.ArgumentReadNode.NonLocalArgumentReadNode;
import trufflesom.interpreter.nodes.ArgumentReadNode.NonLocalArgumentWriteNode;
import trufflesom.interpreter.nodes.ExpressionNode;
import trufflesom.interpreter.nodes.LocalVariableNodeFactory.LocalVariableReadNodeGen;
import trufflesom.interpreter.nodes.LocalVariableNodeFactory.LocalVariableWriteNodeGen;
import trufflesom.interpreter.nodes.NonLocalVariableNodeFactory.NonLocalVariableReadNodeGen;
import trufflesom.interpreter.nodes.NonLocalVariableNodeFactory.NonLocalVariableWriteNodeGen;
import trufflesom.vm.NotYetImplementedException;
import trufflesom.vmobjects.SSymbol;


public abstract class Variable {
  public final String name;
  public final long   coord;

  Variable(final String name, final long coord) {
    this.name = name;
    this.coord = coord;
  }

  public final String getName() {
    return name;
  }

  /** Gets the name including lexical location. */
  public final String makeQualifiedName(final Source source) {
    return name + SourceCoordinate.getLocationQualifier(source, coord);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + name + ")";
  }

  public abstract ExpressionNode getReadNode(int contextLevel, long coordinate);

  public abstract ExpressionNode getWriteNode(
      int contextLevel, ExpressionNode valueExpr, long coord);

  protected abstract void emitPop(BytecodeMethodGenContext mgenc);

  protected abstract void emitPush(BytecodeMethodGenContext mgenc);

  public abstract Variable split();

  public abstract Local splitToMergeIntoOuterScope(int newSlotIndex);

  @Override
  public boolean equals(final Object o) {
    assert o != null;
    if (o == this) {
      return true;
    }
    if (!(o instanceof Variable)) {
      return false;
    }
    Variable var = (Variable) o;
    if (var.coord == coord) {
      assert name == var.name : "Defined in the same place, but names not equal?";
      return true;
    }
    assert coord == 0
        || coord != var.coord : "Why are there multiple objects for this source section? might need to fix comparison above";
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, coord);
  }

  public static final class Argument extends Variable {
    public final int index;

    Argument(final String name, final int index, final long coord) {
      super(name, coord);
      this.index = index;
    }

    public boolean isSelf() {
      return strSelf.equals(name) || strBlockSelf.equals(name);
    }

    @Override
    public Variable split() {
      return this;
    }

    @Override
    public Local splitToMergeIntoOuterScope(final int newSlotIndex) {
      if (isSelf()) {
        return null;
      }

      return new Local(name, coord, newSlotIndex);
    }

    @Override
    public ExpressionNode getReadNode(final int contextLevel, final long coordinate) {
      if (contextLevel == 0) {
        return new LocalArgumentReadNode(this).initialize(coordinate);
      } else {
        return new NonLocalArgumentReadNode(this, contextLevel).initialize(coordinate);
      }
    }

    public ExpressionNode getWriteNode(final int contextLevel,
        final ExpressionNode valueExpr, final long coordinate) {
      if (contextLevel == 0) {
        return new LocalArgumentWriteNode(this, valueExpr).initialize(coordinate);
      } else {
        return new NonLocalArgumentWriteNode(this, contextLevel, valueExpr).initialize(
            coordinate);
      }
    }

    @Override
    public void emitPop(final BytecodeMethodGenContext mgenc) {
      emitPOPARGUMENT(mgenc, (byte) index, (byte) mgenc.getContextLevel(this));
    }

    @Override
    protected void emitPush(final BytecodeMethodGenContext mgenc) {
      emitPUSHARGUMENT(mgenc, (byte) index, (byte) mgenc.getContextLevel(this));
    }
  }

  public static class Local extends Variable {
    protected final int slotIndex;

    @CompilationFinal private FrameDescriptor descriptor;

    Local(final String name, final long coord, final int index) {
      super(name, coord);
      this.slotIndex = index;
    }

    Local(final String name, final long coord, final FrameDescriptor descriptor,
        final int index) {
      super(name, coord);
      this.slotIndex = index;
      this.descriptor = descriptor;
    }

    public void init(final FrameDescriptor desc) {
      this.descriptor = desc;
    }

    @Override
    public ExpressionNode getReadNode(final int contextLevel, final long coordinate) {
      if (contextLevel > 0) {
        return NonLocalVariableReadNodeGen.create(contextLevel, this).initialize(coordinate);
      }
      return LocalVariableReadNodeGen.create(this).initialize(coordinate);
    }

    public final int getIndex() {
      return slotIndex;
    }

    @Override
    public Local split() {
      return new Local(name, coord, slotIndex);
    }

    @Override
    public Local splitToMergeIntoOuterScope(final int newSlotIndex) {
      return new Local(name, coord, newSlotIndex);
    }

    public ExpressionNode getWriteNode(final int contextLevel,
        final ExpressionNode valueExpr, final long coordinate) {
      if (contextLevel > 0) {
        return NonLocalVariableWriteNodeGen.create(contextLevel, this, valueExpr)
                                           .initialize(coordinate);
      }
      return LocalVariableWriteNodeGen.create(this, valueExpr).initialize(coordinate);
    }

    public final FrameDescriptor getFrameDescriptor() {
      assert descriptor != null : "Locals need to be initialized with a frame descriptior. Call init(.) first!";
      return descriptor;
    }

    @Override
    public void emitPop(final BytecodeMethodGenContext mgenc) {
      int contextLevel = mgenc.getContextLevel(this);
      emitPOPLOCAL(mgenc, (byte) slotIndex, (byte) contextLevel);
    }

    @Override
    public void emitPush(final BytecodeMethodGenContext mgenc) {
      int contextLevel = mgenc.getContextLevel(this);
      emitPUSHLOCAL(mgenc, (byte) slotIndex, (byte) contextLevel);
    }
  }

  public static final class Internal extends Local {
    public Internal(final String name, final long coord, final int slotIndex) {
      super(name, coord, slotIndex);
    }

    public Internal(final String name, final long coord,
        final FrameDescriptor descriptor, final int slotIndex) {
      super(name, coord, descriptor, slotIndex);
    }

    @Override
    public ExpressionNode getReadNode(final int contextLevel, final long coordinate) {
      throw new UnsupportedOperationException(
          "There shouldn't be any language-level read nodes for internal slots. "
              + "They are used directly by other nodes.");
    }

    @Override
    public Internal split() {
      return new Internal(name, coord, slotIndex);
    }

    @Override
    public Local splitToMergeIntoOuterScope(final int newSlotIndex) {
      return null;
    }

    @Override
    public void emitPop(final BytecodeMethodGenContext mgenc) {
      throw new NotYetImplementedException();
    }

    @Override
    public void emitPush(final BytecodeMethodGenContext mgenc) {
      throw new NotYetImplementedException();
    }

    @Override
    public boolean equals(final Object o) {
      assert o != null;
      if (o == this) {
        return true;
      }
      if (!(o instanceof Variable)) {
        return false;
      }
      Variable var = (Variable) o;
      return var.coord == coord && name == var.name;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, coord);
    }
  }
}
