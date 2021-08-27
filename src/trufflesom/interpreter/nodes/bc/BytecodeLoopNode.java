package trufflesom.interpreter.nodes.bc;

import static trufflesom.compiler.bc.BytecodeGenerator.emit1;
import static trufflesom.compiler.bc.BytecodeGenerator.emit3;
import static trufflesom.compiler.bc.BytecodeGenerator.emit3WithDummy;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPOP;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPOPFIELD;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPOPLOCAL;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPUSHARGUMENT;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPUSHBLOCK;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPUSHCONSTANT;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPUSHFIELD;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPUSHGLOBAL;
import static trufflesom.compiler.bc.BytecodeGenerator.emitPUSHLOCAL;
import static trufflesom.compiler.bc.BytecodeGenerator.emitRETURNLOCAL;
import static trufflesom.compiler.bc.BytecodeGenerator.emitRETURNNONLOCAL;
import static trufflesom.compiler.bc.BytecodeGenerator.emitSEND;
import static trufflesom.compiler.bc.BytecodeGenerator.emitSUPERSEND;
import static trufflesom.compiler.bc.BytecodeMethodGenContext.getJumpOffset;
import static trufflesom.interpreter.bc.Bytecodes.DEC;
import static trufflesom.interpreter.bc.Bytecodes.DUP;
import static trufflesom.interpreter.bc.Bytecodes.DUP_SECOND;
import static trufflesom.interpreter.bc.Bytecodes.HALT;
import static trufflesom.interpreter.bc.Bytecodes.INC;
import static trufflesom.interpreter.bc.Bytecodes.INC_FIELD;
import static trufflesom.interpreter.bc.Bytecodes.INC_FIELD_PUSH;
import static trufflesom.interpreter.bc.Bytecodes.JUMP;
import static trufflesom.interpreter.bc.Bytecodes.JUMP2;
import static trufflesom.interpreter.bc.Bytecodes.JUMP2_BACKWARDS;
import static trufflesom.interpreter.bc.Bytecodes.JUMP2_IF_GREATER;
import static trufflesom.interpreter.bc.Bytecodes.JUMP2_ON_FALSE_POP;
import static trufflesom.interpreter.bc.Bytecodes.JUMP2_ON_FALSE_TOP_NIL;
import static trufflesom.interpreter.bc.Bytecodes.JUMP2_ON_TRUE_POP;
import static trufflesom.interpreter.bc.Bytecodes.JUMP2_ON_TRUE_TOP_NIL;
import static trufflesom.interpreter.bc.Bytecodes.JUMP_BACKWARDS;
import static trufflesom.interpreter.bc.Bytecodes.JUMP_IF_GREATER;
import static trufflesom.interpreter.bc.Bytecodes.JUMP_ON_FALSE_POP;
import static trufflesom.interpreter.bc.Bytecodes.JUMP_ON_FALSE_TOP_NIL;
import static trufflesom.interpreter.bc.Bytecodes.JUMP_ON_TRUE_POP;
import static trufflesom.interpreter.bc.Bytecodes.JUMP_ON_TRUE_TOP_NIL;
import static trufflesom.interpreter.bc.Bytecodes.POP;
import static trufflesom.interpreter.bc.Bytecodes.POP_ARGUMENT;
import static trufflesom.interpreter.bc.Bytecodes.POP_FIELD;
import static trufflesom.interpreter.bc.Bytecodes.POP_FIELD_0;
import static trufflesom.interpreter.bc.Bytecodes.POP_FIELD_1;
import static trufflesom.interpreter.bc.Bytecodes.POP_LOCAL;
import static trufflesom.interpreter.bc.Bytecodes.POP_LOCAL_0;
import static trufflesom.interpreter.bc.Bytecodes.POP_LOCAL_1;
import static trufflesom.interpreter.bc.Bytecodes.POP_LOCAL_2;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_0;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_1;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_ARG1;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_ARG2;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_ARGUMENT;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_BLOCK;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_BLOCK_NO_CTX;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_CONSTANT;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_CONSTANT_0;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_CONSTANT_1;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_CONSTANT_2;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_FIELD;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_FIELD_0;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_FIELD_1;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_GLOBAL;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_LOCAL;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_LOCAL_0;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_LOCAL_1;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_LOCAL_2;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_NIL;
import static trufflesom.interpreter.bc.Bytecodes.PUSH_SELF;
import static trufflesom.interpreter.bc.Bytecodes.Q_PUSH_GLOBAL;
import static trufflesom.interpreter.bc.Bytecodes.Q_SEND;
import static trufflesom.interpreter.bc.Bytecodes.Q_SEND_1;
import static trufflesom.interpreter.bc.Bytecodes.Q_SEND_2;
import static trufflesom.interpreter.bc.Bytecodes.Q_SEND_3;
import static trufflesom.interpreter.bc.Bytecodes.RETURN_FIELD_0;
import static trufflesom.interpreter.bc.Bytecodes.RETURN_FIELD_1;
import static trufflesom.interpreter.bc.Bytecodes.RETURN_FIELD_2;
import static trufflesom.interpreter.bc.Bytecodes.RETURN_LOCAL;
import static trufflesom.interpreter.bc.Bytecodes.RETURN_NON_LOCAL;
import static trufflesom.interpreter.bc.Bytecodes.RETURN_SELF;
import static trufflesom.interpreter.bc.Bytecodes.SEND;
import static trufflesom.interpreter.bc.Bytecodes.SUPER_SEND;
import static trufflesom.interpreter.bc.Bytecodes.getBytecodeLength;
import static trufflesom.interpreter.bc.Bytecodes.getBytecodeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.profiles.ValueProfile;

import bd.inlining.ScopeAdaptationVisitor;
import bd.inlining.ScopeAdaptationVisitor.ScopeElement;
import bd.inlining.nodes.ScopeReference;
import bd.primitives.Specializer;
import bd.primitives.nodes.PreevaluatedExpression;
import trufflesom.compiler.Parser.ParseError;
import trufflesom.compiler.Variable.Argument;
import trufflesom.compiler.Variable.Local;
import trufflesom.compiler.bc.BytecodeMethodGenContext;
import trufflesom.interpreter.EscapedBlockException;
import trufflesom.interpreter.FrameOnStackMarker;
import trufflesom.interpreter.Invokable;
import trufflesom.interpreter.LexicalScope;
import trufflesom.interpreter.Method;
import trufflesom.interpreter.ReturnException;
import trufflesom.interpreter.SArguments;
import trufflesom.interpreter.Types;
import trufflesom.interpreter.bc.Bytecodes;
import trufflesom.interpreter.bc.RestartLoopException;
import trufflesom.interpreter.nodes.ExpressionNode;
import trufflesom.interpreter.nodes.GlobalNode;
import trufflesom.interpreter.nodes.MessageSendNode;
import trufflesom.interpreter.nodes.MessageSendNode.AbstractMessageSendNode;
import trufflesom.interpreter.nodes.MessageSendNode.GenericMessageSendNode;
import trufflesom.interpreter.nodes.bc.LocalNode.LocalPop;
import trufflesom.interpreter.nodes.bc.LocalNode.LocalPush;
import trufflesom.interpreter.nodes.bc.LocalNodeFactory.LocalPopNodeGen;
import trufflesom.interpreter.nodes.bc.LocalNodeFactory.LocalPushNodeGen;
import trufflesom.interpreter.nodes.literals.IntegerLiteralNode;
import trufflesom.interpreter.nodes.literals.LiteralNode;
import trufflesom.interpreter.nodes.nary.BinaryExpressionNode;
import trufflesom.interpreter.nodes.nary.TernaryExpressionNode;
import trufflesom.interpreter.nodes.nary.UnaryExpressionNode;
import trufflesom.interpreter.objectstorage.FieldAccessorNode;
import trufflesom.interpreter.objectstorage.FieldAccessorNode.AbstractReadFieldNode;
import trufflesom.interpreter.objectstorage.FieldAccessorNode.AbstractWriteFieldNode;
import trufflesom.interpreter.objectstorage.FieldAccessorNode.IncrementLongFieldNode;
import trufflesom.primitives.Primitives;
import trufflesom.vm.NotYetImplementedException;
import trufflesom.vm.Universe;
import trufflesom.vm.constants.Nil;
import trufflesom.vmobjects.SAbstractObject;
import trufflesom.vmobjects.SBlock;
import trufflesom.vmobjects.SClass;
import trufflesom.vmobjects.SInvokable;
import trufflesom.vmobjects.SInvokable.SMethod;
import trufflesom.vmobjects.SObject;
import trufflesom.vmobjects.SSymbol;


public class BytecodeLoopNode extends ExpressionNode implements ScopeReference {
  private static final ValueProfile frameType = ValueProfile.createClassProfile();
  private static final LiteralNode  dummyNode = new IntegerLiteralNode(0);

  @CompilationFinal(dimensions = 1) private final byte[]   bytecodes;
  @CompilationFinal(dimensions = 1) private final Object[] literalsAndConstants;

  @CompilationFinal(dimensions = 1) private Local[] localsAndOuters;

  @CompilationFinal(dimensions = 1) private final BackJump[] inlinedLoops;

  @Children private final Node[] quickened;

  private final int      numLocals;
  private final int      maxStackDepth;
  private final Universe universe;

  private final FrameSlot frameOnStackMarker;

  private final LexicalScope scope;

  public BytecodeLoopNode(final byte[] bytecodes, final int numLocals,
      final Local[] localsAndOuters, final Object[] literals, final int maxStackDepth,
      final FrameSlot frameOnStackMarker, final BackJump[] inlinedLoops,
      final LexicalScope scope, final Universe universe) {
    this.bytecodes = bytecodes;
    this.numLocals = numLocals;
    this.localsAndOuters = localsAndOuters;
    this.literalsAndConstants = literals;
    this.maxStackDepth = maxStackDepth;
    this.inlinedLoops = inlinedLoops;
    this.scope = scope;
    this.universe = universe;

    this.frameOnStackMarker = frameOnStackMarker;

    this.quickened = new Node[bytecodes.length];
  }

  @Override
  public Node deepCopy() {
    return new BytecodeLoopNode(bytecodes.clone(), numLocals, localsAndOuters,
        literalsAndConstants, maxStackDepth, frameOnStackMarker, inlinedLoops, scope,
        universe).initialize(sourceSection);
  }

  public String getNameOfLocal(final int idx) {
    Local l = localsAndOuters[idx];
    return l.name.getString();
  }

  @ExplodeLoop
  private VirtualFrame determineOuterContext(final VirtualFrame frame) {
    // TODO: change bytecode format to include the context level
    Object object = frame.getArguments()[0];

    if (!(object instanceof SBlock)) {
      return frame;
    }

    SBlock self = (SBlock) object;
    MaterializedFrame outer = self.getContext();

    while (true) {
      Object rcvr = outer.getArguments()[0];

      if (rcvr instanceof SBlock) {
        outer = ((SBlock) rcvr).getContext();
      } else {
        return outer;
      }
    }
  }

  @ExplodeLoop
  private MaterializedFrame determineContext(final VirtualFrame frame,
      final int contextLevel) {
    SBlock self = (SBlock) frame.getArguments()[0];
    int i = contextLevel - 1;

    while (i > 0) {
      self = (SBlock) self.getOuterSelf();
      i--;
    }

    // Graal needs help here to see that this is always a MaterializedFrame
    // so, we record explicitly a class profile
    return frameType.profile(self.getContext());
  }

  public void requicken(final int bytecodeIndex, final byte bytecode, final Node node) {
    bytecodes[bytecodeIndex] = bytecode;
    quickened[bytecodeIndex] = insert(node);
  }

  @Override
  @ExplodeLoop(kind = LoopExplosionKind.MERGE_EXPLODE)
  public Object executeGeneric(final VirtualFrame frame) {
    Object[] stack = new Object[maxStackDepth];
    int stackPointer = -1;
    int bytecodeIndex = 0;

    int backBranchesTaken = 0;

    while (true) {
      byte bytecode = bytecodes[bytecodeIndex];
      final int bytecodeLength = getBytecodeLength(bytecode);
      int nextBytecodeIndex = bytecodeIndex + bytecodeLength;

      CompilerAsserts.partialEvaluationConstant(bytecodeIndex);
      CompilerAsserts.partialEvaluationConstant(bytecode);

      switch (bytecode) {
        case HALT: {
          return stack[stackPointer];
        }

        case DUP: {
          Object top = stack[stackPointer];
          stackPointer += 1;
          stack[stackPointer] = top;
          break;
        }

        case DUP_SECOND: {
          Object second = stack[stackPointer - 1];
          stackPointer += 1;
          stack[stackPointer] = second;
          break;
        }

        case PUSH_LOCAL: {
          if (quickened[bytecodeIndex] == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = LocalPushNodeGen.create();
          }
          byte localIdx = bytecodes[bytecodeIndex + 1];
          byte contextIdx = bytecodes[bytecodeIndex + 2];

          VirtualFrame currentOrContext = frame;
          if (contextIdx > 0) {
            currentOrContext = determineContext(currentOrContext, contextIdx);
          }

          stackPointer += 1;
          stack[stackPointer] = ((LocalPush) quickened[bytecodeIndex]).execute(
              currentOrContext, localsAndOuters[localIdx]);
          break;
        }

        case PUSH_LOCAL_0: {
          if (quickened[bytecodeIndex] == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = LocalPushNodeGen.create();
          }

          stackPointer += 1;
          stack[stackPointer] =
              ((LocalPush) quickened[bytecodeIndex]).execute(frame, localsAndOuters[0]);
          break;
        }
        case PUSH_LOCAL_1: {
          if (quickened[bytecodeIndex] == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = LocalPushNodeGen.create();
          }

          stackPointer += 1;
          stack[stackPointer] =
              ((LocalPush) quickened[bytecodeIndex]).execute(frame, localsAndOuters[1]);
          break;
        }
        case PUSH_LOCAL_2: {
          if (quickened[bytecodeIndex] == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = LocalPushNodeGen.create();
          }

          stackPointer += 1;
          stack[stackPointer] =
              ((LocalPush) quickened[bytecodeIndex]).execute(frame, localsAndOuters[2]);
          break;
        }

        case PUSH_ARGUMENT: {
          byte argIdx = bytecodes[bytecodeIndex + 1];
          byte contextIdx = bytecodes[bytecodeIndex + 2];
          assert contextIdx >= 0;

          VirtualFrame currentOrContext = frame;
          if (contextIdx > 0) {
            currentOrContext = determineContext(currentOrContext, contextIdx);
          }

          Object value = currentOrContext.getArguments()[argIdx];
          stackPointer += 1;
          stack[stackPointer] = value;
          break;
        }

        case PUSH_SELF: {
          stackPointer += 1;
          stack[stackPointer] = frame.getArguments()[0];
          break;
        }
        case PUSH_ARG1: {
          stackPointer += 1;
          stack[stackPointer] = frame.getArguments()[1];
          break;
        }
        case PUSH_ARG2: {
          stackPointer += 1;
          stack[stackPointer] = frame.getArguments()[2];
          break;
        }

        case PUSH_FIELD_0: {
          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            node = quickened[bytecodeIndex] = insert(FieldAccessorNode.createRead(0));
          }

          stackPointer += 1;
          stack[stackPointer] =
              ((AbstractReadFieldNode) node).read((SObject) frame.getArguments()[0]);
          break;
        }

        case PUSH_FIELD_1: {
          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            node = quickened[bytecodeIndex] = insert(FieldAccessorNode.createRead(1));
          }

          stackPointer += 1;
          stack[stackPointer] =
              ((AbstractReadFieldNode) node).read((SObject) frame.getArguments()[0]);
          break;
        }

        case PUSH_FIELD: {
          byte fieldIdx = bytecodes[bytecodeIndex + 1];
          byte contextIdx = bytecodes[bytecodeIndex + 2];

          VirtualFrame currentOrContext = frame;
          if (contextIdx > 0) {
            currentOrContext = determineContext(currentOrContext, contextIdx);
          }

          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            node = quickened[bytecodeIndex] = insert(FieldAccessorNode.createRead(fieldIdx));
          }

          stackPointer += 1;
          stack[stackPointer] = ((AbstractReadFieldNode) node).read(
              (SObject) currentOrContext.getArguments()[0]);
          break;
        }

        case PUSH_BLOCK: {
          SMethod blockMethod = (SMethod) literalsAndConstants[bytecodes[bytecodeIndex + 1]];

          stackPointer += 1;
          stack[stackPointer] = new SBlock(blockMethod,
              universe.getBlockClass(blockMethod.getNumberOfArguments()), frame.materialize());
          break;
        }

        case PUSH_BLOCK_NO_CTX: {
          SMethod blockMethod = (SMethod) literalsAndConstants[bytecodes[bytecodeIndex + 1]];

          stackPointer += 1;
          stack[stackPointer] = new SBlock(blockMethod,
              universe.getBlockClass(blockMethod.getNumberOfArguments()), null);
          break;
        }

        case PUSH_CONSTANT: {
          stackPointer += 1;
          stack[stackPointer] = literalsAndConstants[bytecodes[bytecodeIndex + 1]];
          break;
        }

        case PUSH_CONSTANT_0: {
          stackPointer += 1;
          stack[stackPointer] = literalsAndConstants[0];
          break;
        }

        case PUSH_CONSTANT_1: {
          stackPointer += 1;
          stack[stackPointer] = literalsAndConstants[1];
          break;
        }

        case PUSH_CONSTANT_2: {
          stackPointer += 1;
          stack[stackPointer] = literalsAndConstants[2];
          break;
        }

        case PUSH_0: {
          stackPointer += 1;
          stack[stackPointer] = 0L;
          break;
        }

        case PUSH_1: {
          stackPointer += 1;
          stack[stackPointer] = 1L;
          break;
        }

        case PUSH_NIL: {
          stackPointer += 1;
          stack[stackPointer] = Nil.nilObject;
          break;
        }

        case PUSH_GLOBAL: {
          CompilerDirectives.transferToInterpreterAndInvalidate();

          byte literalIdx = bytecodes[bytecodeIndex + 1];
          SSymbol globalName = (SSymbol) literalsAndConstants[literalIdx];

          GlobalNode quickened =
              GlobalNode.create(globalName, universe, null).initialize(sourceSection);
          quickenBytecode(bytecodeIndex, Q_PUSH_GLOBAL, quickened);

          stackPointer += 1;
          stack[stackPointer] = quickened.executeGeneric(frame);
          break;
        }

        case POP: {
          stackPointer -= 1;
          break;
        }

        case POP_LOCAL: {
          if (quickened[bytecodeIndex] == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = LocalPopNodeGen.create();
          }

          byte localIdx = bytecodes[bytecodeIndex + 1];
          byte contextIdx = bytecodes[bytecodeIndex + 2];

          VirtualFrame currentOrContext = frame;
          if (contextIdx > 0) {
            currentOrContext = determineContext(currentOrContext, contextIdx);
          }

          ((LocalPop) quickened[bytecodeIndex]).execute(currentOrContext,
              localsAndOuters[localIdx], stack[stackPointer]);
          stackPointer -= 1;
          break;
        }

        case POP_LOCAL_0: {
          if (quickened[bytecodeIndex] == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = LocalPopNodeGen.create();
          }

          ((LocalPop) quickened[bytecodeIndex]).execute(frame, localsAndOuters[0],
              stack[stackPointer]);
          stackPointer -= 1;
          break;
        }
        case POP_LOCAL_1: {
          if (quickened[bytecodeIndex] == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = LocalPopNodeGen.create();
          }

          ((LocalPop) quickened[bytecodeIndex]).execute(frame, localsAndOuters[1],
              stack[stackPointer]);
          stackPointer -= 1;
          break;
        }
        case POP_LOCAL_2: {
          if (quickened[bytecodeIndex] == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = LocalPopNodeGen.create();
          }

          ((LocalPop) quickened[bytecodeIndex]).execute(frame, localsAndOuters[2],
              stack[stackPointer]);
          stackPointer -= 1;
          break;
        }

        case POP_ARGUMENT: {
          byte argIdx = bytecodes[bytecodeIndex + 1];
          byte contextIdx = bytecodes[bytecodeIndex + 2];

          VirtualFrame currentOrContext = frame;
          if (contextIdx > 0) {
            currentOrContext = determineContext(currentOrContext, contextIdx);
          }

          currentOrContext.getArguments()[argIdx] = stack[stackPointer];
          stackPointer -= 1;
          break;
        }

        case POP_FIELD: {
          byte fieldIdx = bytecodes[bytecodeIndex + 1];
          byte contextIdx = bytecodes[bytecodeIndex + 2];

          VirtualFrame currentOrContext = frame;
          if (contextIdx > 0) {
            currentOrContext = determineContext(currentOrContext, contextIdx);
          }

          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = node = insert(FieldAccessorNode.createWrite(fieldIdx));
          }

          ((AbstractWriteFieldNode) node).write((SObject) currentOrContext.getArguments()[0],
              stack[stackPointer]);
          stackPointer -= 1;
          break;
        }

        case POP_FIELD_0: {
          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = node = insert(FieldAccessorNode.createWrite(0));
          }

          ((AbstractWriteFieldNode) node).write((SObject) frame.getArguments()[0],
              stack[stackPointer]);

          stackPointer -= 1;
          break;
        }
        case POP_FIELD_1: {
          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            quickened[bytecodeIndex] = node = insert(FieldAccessorNode.createWrite(1));
          }

          ((AbstractWriteFieldNode) node).write((SObject) frame.getArguments()[0],
              stack[stackPointer]);

          stackPointer -= 1;
          break;
        }

        case SEND: {
          CompilerDirectives.transferToInterpreterAndInvalidate();
          try {
            byte literalIdx = bytecodes[bytecodeIndex + 1];
            SSymbol signature = (SSymbol) literalsAndConstants[literalIdx];
            int numberOfArguments = signature.getNumberOfSignatureArguments();

            Object[] callArgs = new Object[numberOfArguments];
            System.arraycopy(stack, stackPointer - numberOfArguments + 1, callArgs, 0,
                numberOfArguments);
            stackPointer -= numberOfArguments;

            Object result = null;
            boolean done = false;

            if (numberOfArguments <= 3) {
              Primitives prims = universe.getPrimitives();
              ExpressionNode[] dummyArgs = new ExpressionNode[numberOfArguments];
              Arrays.fill(dummyArgs, dummyNode);

              Specializer<Universe, ExpressionNode, SSymbol> specializer =
                  prims.getEagerSpecializer(signature, callArgs, dummyArgs);

              if (specializer != null) {
                done = true;
                ExpressionNode quickened = specializer.create(callArgs, dummyArgs,
                    sourceSection, !specializer.noWrapper(), universe);

                if (numberOfArguments == 1) {
                  UnaryExpressionNode q = (UnaryExpressionNode) quickened;
                  if (!specializer.noWrapper()) {
                    quickened = q = new UnaryPrimitiveWrapper(
                        bytecodeIndex, signature, q, universe, sourceSection);
                  }
                  quickenBytecode(bytecodeIndex, Q_SEND_1, quickened);
                  result = q.executeEvaluated(frame, callArgs[0]);
                } else if (numberOfArguments == 2) {
                  BinaryExpressionNode q = (BinaryExpressionNode) quickened;

                  if (!specializer.noWrapper()) {
                    quickened = q = new BinaryPrimitiveWrapper(
                        bytecodeIndex, signature, q, universe, sourceSection);
                  }
                  quickenBytecode(bytecodeIndex, Q_SEND_2, quickened);
                  result = q.executeEvaluated(frame, callArgs[0], callArgs[1]);
                } else if (numberOfArguments == 3) {
                  TernaryExpressionNode q = (TernaryExpressionNode) quickened;

                  if (!specializer.noWrapper()) {
                    quickened = q = new TernaryPrimitiveWrapper(
                        bytecodeIndex, signature, q, universe, sourceSection);
                  }
                  quickenBytecode(bytecodeIndex, Q_SEND_3, quickened);
                  result = q.executeEvaluated(frame, callArgs[0], callArgs[1], callArgs[2]);
                }
              }
            }

            if (!done) {
              GenericMessageSendNode quickened =
                  MessageSendNode.createGeneric(signature, null, sourceSection, universe);
              quickenBytecode(bytecodeIndex, Q_SEND, quickened);

              result = quickened.doPreEvaluated(frame, callArgs);
            }

            stackPointer += 1;
            stack[stackPointer] = result;
          } catch (RestartLoopException e) {
            nextBytecodeIndex = 0;
            stackPointer = -1;
          } catch (EscapedBlockException e) {
            CompilerDirectives.transferToInterpreter();
            VirtualFrame outer = determineOuterContext(frame);
            SObject sendOfBlockValueMsg = (SObject) outer.getArguments()[0];
            Object result =
                SAbstractObject.sendEscapedBlock(sendOfBlockValueMsg, e.getBlock(), universe);

            stackPointer += 1;
            stack[stackPointer] = result;
          }
          break;
        }

        case SUPER_SEND: {
          CompilerDirectives.transferToInterpreterAndInvalidate();
          try {
            byte literalIdx = bytecodes[bytecodeIndex + 1];
            SSymbol signature = (SSymbol) literalsAndConstants[literalIdx];
            int numberOfArguments = signature.getNumberOfSignatureArguments();

            Object[] callArgs = new Object[numberOfArguments];
            System.arraycopy(stack, stackPointer - numberOfArguments + 1, callArgs, 0,
                numberOfArguments);
            stackPointer -= numberOfArguments;

            PreevaluatedExpression quickened = MessageSendNode.createSuperSend(
                (SClass) getHolder().getSuperClass(), signature, null, sourceSection);
            quickenBytecode(bytecodeIndex, Q_SEND, (Node) quickened);

            Object result = quickened.doPreEvaluated(frame, callArgs);

            stackPointer += 1;
            stack[stackPointer] = result;
          } catch (RestartLoopException e) {
            nextBytecodeIndex = 0;
            stackPointer = -1;
          } catch (EscapedBlockException e) {
            CompilerDirectives.transferToInterpreter();
            VirtualFrame outer = determineOuterContext(frame);
            SObject sendOfBlockValueMsg = (SObject) outer.getArguments()[0];

            Object result =
                SAbstractObject.sendEscapedBlock(sendOfBlockValueMsg, e.getBlock(), universe);

            stackPointer += 1;
            stack[stackPointer] = result;
          }
          break;
        }

        case RETURN_LOCAL: {
          LoopNode.reportLoopCount(this, backBranchesTaken);
          return stack[stackPointer];
        }

        case RETURN_NON_LOCAL: {
          LoopNode.reportLoopCount(this, backBranchesTaken);

          Object result = stack[stackPointer];
          // stackPointer -= 1;
          doReturnNonLocal(frame, bytecodeIndex, result);
          break;
        }

        case RETURN_SELF: {
          LoopNode.reportLoopCount(this, backBranchesTaken);
          return frame.getArguments()[0];
        }

        case RETURN_FIELD_0: {
          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            node = quickened[bytecodeIndex] = insert(FieldAccessorNode.createRead(0));
          }

          return ((AbstractReadFieldNode) node).read((SObject) frame.getArguments()[0]);
        }
        case RETURN_FIELD_1: {
          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            node = quickened[bytecodeIndex] = insert(FieldAccessorNode.createRead(1));
          }

          return ((AbstractReadFieldNode) node).read((SObject) frame.getArguments()[0]);
        }
        case RETURN_FIELD_2: {
          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            node = quickened[bytecodeIndex] = insert(FieldAccessorNode.createRead(2));
          }

          return ((AbstractReadFieldNode) node).read((SObject) frame.getArguments()[0]);
        }

        case INC: {
          Object top = stack[stackPointer];
          if (top instanceof Long) {
            try {
              stack[stackPointer] = Math.addExact((Long) top, 1L);
            } catch (ArithmeticException e) {
              CompilerDirectives.transferToInterpreterAndInvalidate();
              throw new NotYetImplementedException();
            }
          } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            if (top instanceof Double) {
              stack[stackPointer] = ((Double) top) + 1.0d;
            } else {
              throw new NotYetImplementedException();
            }
          }
          break;
        }

        case DEC: {
          Object top = stack[stackPointer];
          if (top instanceof Long) {
            stack[stackPointer] = ((Long) top) - 1;
          } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            if (top instanceof Double) {
              stack[stackPointer] = ((Double) top) - 1.0d;
            } else {
              throw new NotYetImplementedException();
            }
          }
          break;
        }

        case INC_FIELD: {
          byte fieldIdx = bytecodes[bytecodeIndex + 1];
          byte contextIdx = bytecodes[bytecodeIndex + 2];

          VirtualFrame currentOrContext = frame;
          if (contextIdx > 0) {
            currentOrContext = determineContext(currentOrContext, contextIdx);
          }

          SObject obj = (SObject) currentOrContext.getArguments()[0];

          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Object val = obj.getField(fieldIdx);
            if (!(val instanceof Long)) {
              throw new NotYetImplementedException();
            }

            try {
              long longVal = Math.addExact((Long) val, 1);
              obj.setField(fieldIdx, longVal);
            } catch (ArithmeticException e) {
              throw new NotYetImplementedException();
            }

            node = quickened[bytecodeIndex] =
                insert(FieldAccessorNode.createIncrement(fieldIdx, obj));
            break;
          }

          ((IncrementLongFieldNode) node).increment(obj);
          break;
        }

        case INC_FIELD_PUSH: {
          byte fieldIdx = bytecodes[bytecodeIndex + 1];
          byte contextIdx = bytecodes[bytecodeIndex + 2];

          VirtualFrame currentOrContext = frame;
          if (contextIdx > 0) {
            currentOrContext = determineContext(currentOrContext, contextIdx);
          }

          SObject obj = (SObject) currentOrContext.getArguments()[0];

          Node node = quickened[bytecodeIndex];
          if (node == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Object val = obj.getField(fieldIdx);
            if (!(val instanceof Long)) {
              throw new NotYetImplementedException();
            }

            try {
              long longVal = Math.addExact((Long) val, 1);
              obj.setField(fieldIdx, longVal);
              stackPointer += 1;
              stack[stackPointer] = longVal;
            } catch (ArithmeticException e) {
              throw new NotYetImplementedException();
            }

            node = quickened[bytecodeIndex] =
                insert(FieldAccessorNode.createIncrement(fieldIdx, obj));
            break;
          }

          long value = ((IncrementLongFieldNode) node).increment(obj);
          stackPointer += 1;
          stack[stackPointer] = value;
          break;
        }

        case JUMP: {
          int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1]);
          nextBytecodeIndex = bytecodeIndex + offset;
          break;
        }

        case JUMP_ON_TRUE_TOP_NIL: {
          Object val = stack[stackPointer];
          if (val == Boolean.TRUE) {
            int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1]);
            nextBytecodeIndex = bytecodeIndex + offset;
            stack[stackPointer] = Nil.nilObject;
          } else {
            stackPointer -= 1;
          }
          break;
        }

        case JUMP_ON_FALSE_TOP_NIL: {
          Object val = stack[stackPointer];
          if (val == Boolean.FALSE) {
            int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1]);
            nextBytecodeIndex = bytecodeIndex + offset;
            stack[stackPointer] = Nil.nilObject;
          } else {
            stackPointer -= 1;
          }
          break;
        }

        case JUMP_ON_TRUE_POP: {
          Object val = stack[stackPointer];
          if (val == Boolean.TRUE) {
            int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1]);
            nextBytecodeIndex = bytecodeIndex + offset;
          }
          stackPointer -= 1;
          break;
        }

        case JUMP_ON_FALSE_POP: {
          Object val = stack[stackPointer];
          if (val == Boolean.FALSE) {
            int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1]);
            nextBytecodeIndex = bytecodeIndex + offset;
          }
          stackPointer -= 1;
          break;
        }

        case JUMP_IF_GREATER: {
          long top = (Long) stack[stackPointer];
          long top2 = (Long) stack[stackPointer - 1];

          if (top > top2) {
            stackPointer -= 2;
            int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1]);
            nextBytecodeIndex = bytecodeIndex + offset;
          }

          break;
        }

        case JUMP_BACKWARDS: {
          int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1]);
          nextBytecodeIndex = bytecodeIndex - offset;
          break;
        }

        case JUMP2: {
          int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1])
              + (Byte.toUnsignedInt(bytecodes[bytecodeIndex + 2]) << 8);
          nextBytecodeIndex = bytecodeIndex + offset;

          if (CompilerDirectives.inInterpreter()) {
            backBranchesTaken += 1;
          }
          break;
        }

        case JUMP2_ON_TRUE_TOP_NIL: {
          Object val = stack[stackPointer];
          if (val == Boolean.TRUE) {
            int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1])
                + (Byte.toUnsignedInt(bytecodes[bytecodeIndex + 2]) << 8);
            nextBytecodeIndex = bytecodeIndex + offset;
            stack[stackPointer] = Nil.nilObject;
          } else {
            stackPointer -= 1;
          }
          break;
        }

        case JUMP2_ON_FALSE_TOP_NIL: {
          Object val = stack[stackPointer];
          if (val == Boolean.FALSE) {
            int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1])
                + (Byte.toUnsignedInt(bytecodes[bytecodeIndex + 2]) << 8);
            nextBytecodeIndex = bytecodeIndex + offset;
            stack[stackPointer] = Nil.nilObject;
          } else {
            stackPointer -= 1;
          }
          break;
        }

        case JUMP2_ON_TRUE_POP: {
          Object val = stack[stackPointer];
          if (val == Boolean.TRUE) {
            int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1])
                + (Byte.toUnsignedInt(bytecodes[bytecodeIndex + 2]) << 8);
            nextBytecodeIndex = bytecodeIndex + offset;
          }
          stackPointer -= 1;
          break;
        }

        case JUMP2_ON_FALSE_POP: {
          Object val = stack[stackPointer];
          if (val == Boolean.FALSE) {
            int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1])
                + (Byte.toUnsignedInt(bytecodes[bytecodeIndex + 2]) << 8);
            nextBytecodeIndex = bytecodeIndex + offset;
          }
          stackPointer -= 1;
          break;
        }

        case JUMP2_IF_GREATER: {
          long top = (Long) stack[stackPointer];
          long top2 = (Long) stack[stackPointer - 1];

          if (top > top2) {
            stackPointer -= 2;
            int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1])
                + (Byte.toUnsignedInt(bytecodes[bytecodeIndex + 2]) << 8);
            nextBytecodeIndex = bytecodeIndex + offset;
          }

          break;
        }

        case JUMP2_BACKWARDS: {
          int offset = Byte.toUnsignedInt(bytecodes[bytecodeIndex + 1])
              + (Byte.toUnsignedInt(bytecodes[bytecodeIndex + 2]) << 8);
          nextBytecodeIndex = bytecodeIndex - offset;

          if (CompilerDirectives.inInterpreter()) {
            backBranchesTaken += 1;
          }
          break;
        }

        case Q_PUSH_GLOBAL: {
          stackPointer += 1;
          stack[stackPointer] = ((GlobalNode) quickened[bytecodeIndex]).executeGeneric(frame);
          break;
        }

        case Q_SEND: {
          AbstractMessageSendNode node = (AbstractMessageSendNode) quickened[bytecodeIndex];

          int numberOfArguments =
              node.getInvocationIdentifier().getNumberOfSignatureArguments();

          Object[] callArgs = new Object[numberOfArguments];
          System.arraycopy(stack, stackPointer - numberOfArguments + 1, callArgs, 0,
              numberOfArguments);
          stackPointer -= numberOfArguments;

          try {
            Object result = node.doPreEvaluated(frame, callArgs);

            stackPointer += 1;
            stack[stackPointer] = result;
          } catch (RestartLoopException e) {
            nextBytecodeIndex = 0;
            stackPointer = -1;
          } catch (EscapedBlockException e) {
            CompilerDirectives.transferToInterpreter();
            VirtualFrame outer = determineOuterContext(frame);
            SObject sendOfBlockValueMsg = (SObject) outer.getArguments()[0];
            Object result =
                SAbstractObject.sendEscapedBlock(sendOfBlockValueMsg, e.getBlock(), universe);

            stackPointer += 1;
            stack[stackPointer] = result;
          }

          break;
        }

        case Q_SEND_1: {
          Object rcvr = stack[stackPointer];

          stackPointer -= 1;

          try {
            UnaryExpressionNode node = (UnaryExpressionNode) quickened[bytecodeIndex];
            Object result = node.executeEvaluated(frame, rcvr);

            stackPointer += 1;
            stack[stackPointer] = result;
          } catch (RestartLoopException e) {
            nextBytecodeIndex = 0;
            stackPointer = -1;
          } catch (EscapedBlockException e) {
            CompilerDirectives.transferToInterpreter();
            VirtualFrame outer = determineOuterContext(frame);
            SObject sendOfBlockValueMsg = (SObject) outer.getArguments()[0];
            Object result =
                SAbstractObject.sendEscapedBlock(sendOfBlockValueMsg, e.getBlock(), universe);

            stackPointer += 1;
            stack[stackPointer] = result;
          }

          break;
        }

        case Q_SEND_2: {
          Object rcvr = stack[stackPointer - 1];
          Object arg = stack[stackPointer];

          stackPointer -= 2;

          try {
            BinaryExpressionNode node = (BinaryExpressionNode) quickened[bytecodeIndex];
            Object result = node.executeEvaluated(frame, rcvr, arg);

            stackPointer += 1;
            stack[stackPointer] = result;
          } catch (RestartLoopException e) {
            nextBytecodeIndex = 0;
            stackPointer = -1;
          } catch (EscapedBlockException e) {
            CompilerDirectives.transferToInterpreter();
            VirtualFrame outer = determineOuterContext(frame);
            SObject sendOfBlockValueMsg = (SObject) outer.getArguments()[0];
            Object result =
                SAbstractObject.sendEscapedBlock(sendOfBlockValueMsg, e.getBlock(), universe);

            stackPointer += 1;
            stack[stackPointer] = result;
          }

          break;
        }

        case Q_SEND_3: {
          Object rcvr = stack[stackPointer - 2];
          Object arg1 = stack[stackPointer - 1];
          Object arg2 = stack[stackPointer];

          stackPointer -= 3;

          try {
            TernaryExpressionNode node = (TernaryExpressionNode) quickened[bytecodeIndex];
            Object result = node.executeEvaluated(frame, rcvr, arg1, arg2);

            stackPointer += 1;
            stack[stackPointer] = result;
          } catch (RestartLoopException e) {
            nextBytecodeIndex = 0;
            stackPointer = -1;
          } catch (EscapedBlockException e) {
            CompilerDirectives.transferToInterpreter();
            VirtualFrame outer = determineOuterContext(frame);
            SObject sendOfBlockValueMsg = (SObject) outer.getArguments()[0];
            Object result =
                SAbstractObject.sendEscapedBlock(sendOfBlockValueMsg, e.getBlock(), universe);

            stackPointer += 1;
            stack[stackPointer] = result;
          }

          break;
        }

        default:
          CompilerDirectives.transferToInterpreter();
          throw new NotYetImplementedException("The bytecode " + bytecode + " ("
              + Bytecodes.getBytecodeName(bytecode) + ") is not yet implemented.");
      }

      bytecodeIndex = nextBytecodeIndex;
    }
  }

  private void quickenBytecode(final int bytecodeIndex, final byte quickenedBytecode,
      final Node quickenedNode) {
    this.quickened[bytecodeIndex] = insert(quickenedNode);
    bytecodes[bytecodeIndex] = quickenedBytecode;
  }

  private SClass getHolder() {
    return ((Invokable) getRootNode()).getHolder();
  }

  private void doReturnNonLocal(final VirtualFrame frame, final int bytecodeIndex,
      final Object result) {
    byte contextIdx = bytecodes[bytecodeIndex + 1];

    MaterializedFrame ctx = determineContext(frame, contextIdx);
    FrameOnStackMarker marker =
        (FrameOnStackMarker) FrameUtil.getObjectSafe(ctx, frameOnStackMarker);

    if (marker.isOnStack()) {
      throw new ReturnException(result, marker);
    } else {
      SBlock block = (SBlock) SArguments.rcvr(frame);
      throw new EscapedBlockException(block);
    }
  }

  @TruffleBoundary
  private SInvokable doLookup(final SSymbol signature, final Object[] callArgs) {
    SClass rcvrClass = Types.getClassOf(callArgs[0], universe);
    SInvokable invokable = rcvrClass.lookupInvokable(signature);
    return invokable;
  }

  public int getNumberOfLocals() {
    return numLocals;
  }

  public int getMaximumNumberOfStackElements() {
    return maxStackDepth;
  }

  public int getNumberOfBytecodes() {
    return bytecodes.length;
  }

  public List<Byte> getBytecodes() {
    List<Byte> list = new ArrayList<>(bytecodes.length);
    for (byte b : bytecodes) {
      list.add(b);
    }
    return list;
  }

  public byte[] getBytecodeArray() {
    return bytecodes;
  }

  public Object getConstant(final int idx) {
    return literalsAndConstants[idx];
  }

  @Override
  public void replaceAfterScopeChange(final ScopeAdaptationVisitor inliner) {
    Object scope = inliner.getCurrentScope();
    int targetContextLevel = inliner.contextLevel;

    if (scope instanceof BytecodeMethodGenContext) {
      BytecodeMethodGenContext mgenc = (BytecodeMethodGenContext) scope;

      try {
        inlineInto(mgenc, targetContextLevel);
      } catch (ParseError e) {
        throw new RuntimeException(e);
      }
    } else {
      boolean requiresChangesToContextLevels = inliner.outerScopeChanged();
      adapt(inliner, requiresChangesToContextLevels);
    }
  }

  private static final class Jump implements Comparable<Jump> {
    final byte jumpBc;

    final int originalTarget;
    final int offsetIdx;

    Jump(final byte bc, final int target, final int offsetIdx) {
      this.jumpBc = bc;
      this.originalTarget = target;
      this.offsetIdx = offsetIdx;
      assert target > 0;
    }

    @Override
    public int compareTo(final Jump o) {
      return this.originalTarget - o.originalTarget;
    }

    @Override
    public String toString() {
      return Bytecodes.getBytecodeName(jumpBc) + " -> " + originalTarget;
    }
  }

  public static final class BackJump implements Comparable<BackJump> {
    final int loopBeginIdx;
    final int backwardsJumpIdx;

    public BackJump(final int loopBeginIdx, final int backwardsJumpIdx) {
      this.loopBeginIdx = loopBeginIdx;
      this.backwardsJumpIdx = backwardsJumpIdx;
    }

    @Override
    public int compareTo(final BackJump o) {
      return this.loopBeginIdx - o.loopBeginIdx;
    }

    @Override
    public String toString() {
      return "Loop begin at: " + loopBeginIdx + " -> " + backwardsJumpIdx;
    }
  }

  private static final class BackJumpPatch implements Comparable<BackJumpPatch> {
    final int backwardsJumpIdx;
    final int jumpTargetAddress;

    BackJumpPatch(final int backwardsJumpIdx, final int jumpTargetAddress) {
      this.backwardsJumpIdx = backwardsJumpIdx;
      this.jumpTargetAddress = jumpTargetAddress;
    }

    @Override
    public int compareTo(final BackJumpPatch o) {
      return this.backwardsJumpIdx - o.backwardsJumpIdx;
    }
  }

  private PriorityQueue<BackJump> createBackwardJumpQueue() {
    PriorityQueue<BackJump> loops = new PriorityQueue<>();
    if (inlinedLoops != null) {
      for (BackJump l : inlinedLoops) {
        loops.add(l);
      }
    }
    return loops;
  }

  private void prepareBackJumpToCurrentAddress(final PriorityQueue<BackJump> backJumps,
      final PriorityQueue<BackJumpPatch> backJumpsToPatch, final int i,
      final BytecodeMethodGenContext mgenc) {
    while (backJumps != null && !backJumps.isEmpty() && backJumps.peek().loopBeginIdx <= i) {
      BackJump jump = backJumps.poll();
      assert jump.loopBeginIdx == i : "we use the less or equal, but actually expect it to be strictly equal";
      backJumpsToPatch.add(
          new BackJumpPatch(jump.backwardsJumpIdx, mgenc.offsetOfNextInstruction()));
    }
  }

  private void patchJumpToCurrentAddress(final int i, final PriorityQueue<Jump> jumps,
      final BytecodeMethodGenContext mgenc) throws ParseError {
    while (!jumps.isEmpty() && jumps.peek().originalTarget <= i) {
      Jump j = jumps.poll();
      assert j.originalTarget == i : "we use the less or equal, but actually expect it to be strictly equal";
      mgenc.patchJumpOffsetToPointToNextInstruction(j.offsetIdx, null);
    }
  }

  private void inlineInto(final BytecodeMethodGenContext mgenc, final int targetContextLevel)
      throws ParseError {
    PriorityQueue<Jump> jumps = new PriorityQueue<>();
    PriorityQueue<BackJump> loops = createBackwardJumpQueue();
    PriorityQueue<BackJumpPatch> backJumps = new PriorityQueue<>();

    int i = 0;
    while (i < bytecodes.length) {
      prepareBackJumpToCurrentAddress(loops, backJumps, i, mgenc);
      patchJumpToCurrentAddress(i, jumps, mgenc);

      byte bytecode = bytecodes[i];
      final int bytecodeLength = getBytecodeLength(bytecode);

      switch (bytecode) {
        case HALT:
        case DUP:
        case DUP_SECOND: {
          emit1(mgenc, bytecode, bytecode == HALT ? 0 : 1);
          break;
        }

        case PUSH_LOCAL: {
          byte localIdx = bytecodes[i + 1];
          localsAndOuters[localIdx].emitPush(mgenc);
          break;
        }

        case PUSH_LOCAL_0:
        case PUSH_LOCAL_1:
        case PUSH_LOCAL_2: {
          byte localIdx = (byte) (bytecode - PUSH_LOCAL_0);
          localsAndOuters[localIdx].emitPush(mgenc);
          break;
        }

        case PUSH_ARGUMENT: {
          byte argIdx = bytecodes[i + 1];
          byte contextIdx = bytecodes[i + 2];
          emitPUSHARGUMENT(mgenc, argIdx, (byte) (contextIdx - 1));
          break;
        }

        case PUSH_FIELD: {
          byte fieldIdx = bytecodes[i + 1];
          byte contextIdx = bytecodes[i + 2];
          emitPUSHFIELD(mgenc, fieldIdx, (byte) (contextIdx - 1));
          break;
        }

        case PUSH_ARG1: {
          Argument arg1 = (Argument) scope.getVariable(1, 0);
          byte varIdx = mgenc.getVarIndex(arg1);
          emitPUSHLOCAL(mgenc, varIdx, (byte) 0);
          break;
        }

        case PUSH_SELF:
        case PUSH_ARG2:
        case PUSH_FIELD_0:
        case PUSH_FIELD_1: {
          throw new IllegalStateException("contextLevel is 0, so, not expected to be here");
        }

        case PUSH_BLOCK:
        case PUSH_BLOCK_NO_CTX: {
          byte literalIdx = bytecodes[i + 1];
          SMethod blockMethod = (SMethod) literalsAndConstants[literalIdx];

          Method blockIvk = (Method) blockMethod.getInvokable();
          Method adapted = blockIvk.cloneAndAdaptAfterScopeChange(null,
              mgenc.getCurrentLexicalScope().getScope(blockIvk),
              targetContextLevel + 1, true, true);
          SMethod newMethod = new SMethod(blockMethod.getSignature(), adapted,
              blockMethod.getEmbeddedBlocks(), blockIvk.getSourceSection());
          newMethod.setHolder(blockMethod.getHolder());
          mgenc.addLiteralIfAbsent(newMethod, null);
          emitPUSHBLOCK(mgenc, newMethod, bytecodes[i] == PUSH_BLOCK);
          break;
        }

        case PUSH_CONSTANT: {
          byte literalIdx = bytecodes[i + 1];
          Object value = literalsAndConstants[literalIdx];
          emitPUSHCONSTANT(mgenc, value, null);
          break;
        }

        case PUSH_CONSTANT_0:
        case PUSH_CONSTANT_1:
        case PUSH_CONSTANT_2: {
          int literalIdx = bytecode - PUSH_CONSTANT_0;
          emitPUSHCONSTANT(mgenc, literalsAndConstants[literalIdx], null);
          break;
        }

        case PUSH_0:
        case PUSH_1:
        case PUSH_NIL: {
          emit1(mgenc, bytecode, 1);
          break;
        }

        case PUSH_GLOBAL: {
          byte literalIdx = bytecodes[i + 1];
          SSymbol globalName = (SSymbol) literalsAndConstants[literalIdx];
          emitPUSHGLOBAL(mgenc, globalName, null);
          break;
        }

        case POP: {
          emitPOP(mgenc);
          break;
        }

        case POP_LOCAL: {
          byte localIdx = bytecodes[i + 1];
          localsAndOuters[localIdx].emitPop(mgenc);
          break;
        }

        case POP_LOCAL_0:
        case POP_LOCAL_1:
        case POP_LOCAL_2: {
          byte localIdx = (byte) (bytecode - POP_LOCAL_0);
          localsAndOuters[localIdx].emitPop(mgenc);
          break;
        }

        case POP_ARGUMENT: {
          byte argIdx = bytecodes[i + 1];
          byte ctxLevel = bytecodes[i + 2];

          if (ctxLevel > 0) {
            emit3(mgenc, bytecode, argIdx, (byte) (ctxLevel - 1), -1);
          } else {
            assert ctxLevel == 0;
            // ok, we are currently inlining a block, and we try to store into an argument
            // well, we only have the to:do: blocks, that have arguments in the first place
            // and there's only one
            assert argIdx == 1 : "Don't support storing to any other than the loop index argument";
            Argument arg1 = (Argument) scope.getVariable(1, 0);
            byte varIdx = mgenc.getVarIndex(arg1);
            emitPOPLOCAL(mgenc, varIdx, (byte) 0);
          }
          break;
        }

        case POP_FIELD: {
          byte fieldIdx = bytecodes[i + 1];
          byte contextIdx = bytecodes[i + 2];
          emitPOPFIELD(mgenc, fieldIdx, (byte) (contextIdx - 1));
          break;
        }

        case POP_FIELD_0:
        case POP_FIELD_1: {
          throw new IllegalStateException("contextLevel is 0, so, not expected to be here");
        }

        case SEND: {
          byte literalIdx = bytecodes[i + 1];
          SSymbol signature = (SSymbol) literalsAndConstants[literalIdx];
          emitSEND(mgenc, signature, null);
          break;
        }

        case SUPER_SEND: {
          byte literalIdx = bytecodes[i + 1];
          SSymbol signature = (SSymbol) literalsAndConstants[literalIdx];
          emitSUPERSEND(mgenc, signature, null);
          break;
        }

        case RETURN_LOCAL: {
          // simply don't translate
          assert i == bytecodes.length - 1;
          break;
        }

        case RETURN_NON_LOCAL: {
          byte contextIdx = bytecodes[i + 1];
          byte newCtx = (byte) (contextIdx - 1);
          if (newCtx == 0) {
            emitRETURNLOCAL(mgenc);
          } else {
            emitRETURNNONLOCAL(mgenc);
          }
          break;
        }

        case RETURN_SELF: {
          throw new IllegalStateException(
              "I wouldn't expect RETURN_SELF ever to be inlined, since it's only generated in the most outer methods");
        }

        case RETURN_FIELD_0:
        case RETURN_FIELD_1:
        case RETURN_FIELD_2:
          throw new IllegalStateException(
              "I wouldn't expect RETURN_FIELD_n ever to be inlined, since it's only generated in the most outer methods");

        case INC:
        case DEC: {
          emit1(mgenc, bytecode, 0);
          break;
        }

        case INC_FIELD:
        case INC_FIELD_PUSH: {
          byte fieldIdx = bytecodes[i + 1];
          byte contextIdx = bytecodes[i + 2];
          emit3(mgenc, bytecode, fieldIdx, (byte) (contextIdx - 1), 1);
          break;
        }

        case JUMP:
        case JUMP2:
        case JUMP_ON_TRUE_TOP_NIL:
        case JUMP2_ON_TRUE_TOP_NIL:
        case JUMP_ON_FALSE_TOP_NIL:
        case JUMP2_ON_FALSE_TOP_NIL:
        case JUMP_IF_GREATER:
        case JUMP2_IF_GREATER: {
          int offset = getJumpOffset(bytecodes[i + 1], bytecodes[i + 2]);

          int idxOffset = emit3WithDummy(mgenc, bytecode, 0);
          jumps.add(new Jump(bytecode, offset + i, idxOffset));
          break;
        }

        case JUMP_ON_TRUE_POP:
        case JUMP2_ON_TRUE_POP:
        case JUMP_ON_FALSE_POP:
        case JUMP2_ON_FALSE_POP: {
          int offset = getJumpOffset(bytecodes[i + 1], bytecodes[i + 2]);

          int idxOffset = emit3WithDummy(mgenc, bytecode, -1);
          jumps.add(new Jump(bytecode, offset + i, idxOffset));
          break;
        }

        case JUMP_BACKWARDS:
        case JUMP2_BACKWARDS: {
          BackJumpPatch backJumpPatch = backJumps.poll();
          assert backJumpPatch.backwardsJumpIdx == i : "Jump should match with jump instruction";
          mgenc.emitBackwardsJumpOffsetToTarget(backJumpPatch.jumpTargetAddress, null);
          break;
        }

        default:
          throw new NotYetImplementedException(
              "Support for bytecode " + getBytecodeName(bytecode) + " has not yet been added");
      }

      i += bytecodeLength;
    }

    assert jumps.isEmpty();
  }

  private void adapt(final ScopeAdaptationVisitor inliner,
      final boolean requiresChangesToContextLevels) {
    CompilerAsserts.neverPartOfCompilation("Not a run-time thing, a parse time thing...");
    Local[] oldLocalsAndOuters = Arrays.copyOf(localsAndOuters, localsAndOuters.length);

    int i = 0;
    while (i < bytecodes.length) {
      byte bytecode = bytecodes[i];
      final int bytecodeLength = getBytecodeLength(bytecode);

      switch (bytecode) {
        case HALT:
        case DUP:
        case DUP_SECOND: {
          break;
        }

        case PUSH_LOCAL:
        case PUSH_LOCAL_0:
        case PUSH_LOCAL_1:
        case PUSH_LOCAL_2: {
          byte localIdx;
          if (bytecode == PUSH_LOCAL) {
            localIdx = bytecodes[i + 1];
          } else {
            localIdx = (byte) (bytecode - PUSH_LOCAL_0);
          }
          ScopeElement<ExpressionNode> se =
              inliner.getAdaptedVar(oldLocalsAndOuters[localIdx]);

          if (bytecode == PUSH_LOCAL) {
            bytecodes[i + 2] = (byte) se.contextLevel;
            assert bytecodes[i + 2] >= 0;
          }
          localsAndOuters[localIdx] = (Local) se.var;
          break;
        }

        case PUSH_ARGUMENT: {
          byte argIdx = bytecodes[i + 1];
          byte ctxLevel = bytecodes[i + 2];
          if (ctxLevel == inliner.contextLevel && requiresChangesToContextLevels) {
            if (argIdx == 1) {
              // at this point, because of the context level,
              // we know we refer to a block that got inlined
              // the only time we could possibly refer to the argument
              // for an inlined block is when inlining #to:do:
              // so, there's only the one argument, everything else
              // would mean we have a terrible bug...
              Argument arg1 = (Argument) scope.getVariable(1, ctxLevel);
              ScopeElement<ExpressionNode> se = inliner.getAdaptedVar(arg1);

              assert se.contextLevel == ctxLevel;

              int foundIdx = -1;
              for (int j = 0; j < localsAndOuters.length; j += 1) {
                if (localsAndOuters[j].equals(se.var)) {
                  foundIdx = j;
                  break;
                }
              }

              if (foundIdx == -1) {
                foundIdx = localsAndOuters.length;
                localsAndOuters = Arrays.copyOf(localsAndOuters, localsAndOuters.length + 1);
              }

              localsAndOuters[foundIdx] = (Local) se.var;
              bytecodes[i] = Bytecodes.PUSH_LOCAL;
              bytecodes[i + 1] = (byte) foundIdx;
            } else {
              throw new RuntimeException("This should really never happen");
            }
            break;
          }
          adaptContextIdx(inliner, i, requiresChangesToContextLevels);
          break;
        }

        case PUSH_SELF:
        case PUSH_ARG1:
        case PUSH_ARG2: {
          break;
        }

        case PUSH_FIELD: {
          adaptContextIdx(inliner, i, requiresChangesToContextLevels);
          break;
        }

        case PUSH_FIELD_0:
        case PUSH_FIELD_1: {
          break;
        }

        case PUSH_BLOCK:
        case PUSH_BLOCK_NO_CTX: {
          byte literalIdx = bytecodes[i + 1];
          SMethod blockMethod = (SMethod) literalsAndConstants[literalIdx];

          Method blockIvk = (Method) blockMethod.getInvokable();
          Method adapted =
              blockIvk.cloneAndAdaptAfterScopeChange(null, inliner.getScope(blockIvk),
                  inliner.contextLevel + 1, true, requiresChangesToContextLevels);
          SMethod newMethod = new SMethod(blockMethod.getSignature(), adapted,
              blockMethod.getEmbeddedBlocks(), blockIvk.getSourceSection());
          newMethod.setHolder(blockMethod.getHolder());
          literalsAndConstants[literalIdx] = newMethod;
          break;
        }

        case PUSH_CONSTANT:
        case PUSH_CONSTANT_0:
        case PUSH_CONSTANT_1:
        case PUSH_CONSTANT_2:
        case PUSH_0:
        case PUSH_1:
        case PUSH_NIL:
        case PUSH_GLOBAL:
        case POP: {
          break;
        }

        case POP_LOCAL:
        case POP_LOCAL_0:
        case POP_LOCAL_1:
        case POP_LOCAL_2: {
          byte localIdx;
          if (bytecode == POP_LOCAL) {
            localIdx = bytecodes[i + 1];
          } else {
            localIdx = (byte) (bytecode - POP_LOCAL_0);
          }
          ScopeElement<ExpressionNode> se =
              inliner.getAdaptedVar(oldLocalsAndOuters[localIdx]);

          if (bytecode == POP_LOCAL) {
            bytecodes[i + 2] = (byte) se.contextLevel;
            assert bytecodes[i + 2] >= 0;
          }
          localsAndOuters[localIdx] = (Local) se.var;
          break;
        }

        case POP_ARGUMENT: {
          adaptContextIdx(inliner, i, requiresChangesToContextLevels);
          break;
        }

        case POP_FIELD: {
          adaptContextIdx(inliner, i, requiresChangesToContextLevels);
          break;
        }

        case POP_FIELD_0:
        case POP_FIELD_1:
        case SEND:
        case SUPER_SEND:
        case RETURN_LOCAL: {
          break;
        }

        case RETURN_NON_LOCAL: {
          byte contextIdx = bytecodes[i + 1];
          if (requiresChangesToContextLevels && contextIdx >= inliner.contextLevel) {
            // we don't simplify to return local, because they had different bytecode length
            // and, well, I don't think this should happen
            assert contextIdx - 1 > 0 : "I wouldn't expect a RETURN_LOCAL equivalent here, "
                + " because we are in a block, or it is already a return local";
            bytecodes[i + 1] = (byte) (contextIdx - 1);
          }
          break;
        }

        case RETURN_SELF:
        case RETURN_FIELD_0:
        case RETURN_FIELD_1:
        case RETURN_FIELD_2:
        case INC:
        case DEC: {
          break;
        }

        case INC_FIELD:
        case INC_FIELD_PUSH: {
          adaptContextIdx(inliner, i, requiresChangesToContextLevels);
          break;
        }

        case JUMP:
        case JUMP_ON_TRUE_TOP_NIL:
        case JUMP_ON_FALSE_TOP_NIL:
        case JUMP_ON_TRUE_POP:
        case JUMP_ON_FALSE_POP:
        case JUMP_IF_GREATER:
        case JUMP_BACKWARDS:
        case JUMP2:
        case JUMP2_ON_TRUE_TOP_NIL:
        case JUMP2_ON_FALSE_TOP_NIL:
        case JUMP2_ON_TRUE_POP:
        case JUMP2_ON_FALSE_POP:
        case JUMP2_IF_GREATER:
        case JUMP2_BACKWARDS: {
          break;
        }

        case Q_PUSH_GLOBAL: {
          bytecodes[i] = PUSH_GLOBAL;
          break;
        }

        case Q_SEND:
        case Q_SEND_1:
        case Q_SEND_2:
        case Q_SEND_3: {
          bytecodes[i] = SEND;
          break;
        }

        default:
          throw new NotYetImplementedException(
              "Support for bytecode " + getBytecodeName(bytecode) + " has not yet been added");
      }

      i += bytecodeLength;
    }
  }

  private void adaptContextIdx(final ScopeAdaptationVisitor inliner, final int i,
      final boolean requiresChangesToContextLevels) {
    if (!requiresChangesToContextLevels) {
      return;
    }

    byte contextIdx = bytecodes[i + 2];
    if (contextIdx >= inliner.contextLevel) {
      byte ctx = (byte) (contextIdx - 1);
      assert ctx >= 0;
      bytecodes[i + 2] = ctx;
    }
  }

  @Override
  public String toString() {
    RootNode root = getRootNode();
    if (root == null) {
      return super.toString();
    }
    return getClass().getSimpleName() + "(" + root.getName() + ")";
  }
}
