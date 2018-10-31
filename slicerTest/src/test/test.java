package test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.ConcreteJavaMethod;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.ParamCallee;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.thin.ThinSlicer;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;

public class test {
	Collection<Statement> collection = null;

	public static void print(CallGraph cg) {
		int i = 1;
		for (Iterator<? extends CGNode> it = cg.iterator(); it.hasNext();) {

			CGNode n = it.next(); // 函数名和类名比较
//			System.out.println("method:  " + n.getMethod().getName() + "    class:  "
//					+ n.getMethod().getDeclaringClass().getName().toString());
			System.err.println(n);

		}

	}

	// 打印特定方法在sdg中的statement
	public static void printSDG(SDG<?> sdg, String name) {
		int i = 0;
		for (Iterator<Statement> it = sdg.iterator(); it.hasNext();) {
			i = 0;
			Statement state = it.next(); // 函数名和类名比较
			if (state.getNode().getMethod().getName().toString().contains(name)) {
				if (state.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of statements
					int bcIndex, instructionIndex = ((NormalStatement) state).getInstructionIndex();
					try {
						bcIndex = ((ShrikeBTMethod) state.getNode().getMethod()).getBytecodeIndex(instructionIndex);
						try {
							i = state.getNode().getMethod().getLineNumber(bcIndex);

						} catch (Exception e) {
							System.err.println("Bytecode index no good");
							System.err.println(e.getMessage());
						}
					} catch (Exception e) {
						System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
						i = 1;
						System.err.println(e.getMessage());
					}
				}
				System.out.println("statement:  " + state.toString());
				if (state.getKind() == Statement.Kind.NORMAL)
					System.out.println("----------" + i + ((NormalStatement) state).getInstruction().toString());
				if (state.getKind() == Statement.Kind.PARAM_CALLEE)
					System.out.println("----------" + i + ((ParamCallee) state).getValueNumber());
				System.out.println();
				// System.err.println(n);

			}
		}
	}

	public static void printSDG(SDG<?> sdg) {
		int i = 0;
		for (Iterator<Statement> it = sdg.iterator(); it.hasNext();) {
			i = 0;
			Statement state = it.next(); // 函数名和类名比较

			if (state.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of statements
				int bcIndex, instructionIndex = ((NormalStatement) state).getInstructionIndex();
				try {
					bcIndex = ((ShrikeBTMethod) state.getNode().getMethod()).getBytecodeIndex(instructionIndex);
					try {
						i = state.getNode().getMethod().getLineNumber(bcIndex);

					} catch (Exception e) {
						System.err.println("Bytecode index no good");
						System.err.println(e.getMessage());
					}
				} catch (Exception e) {
					System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
					i = 1;
					System.err.println(e.getMessage());
				}
			}
			System.out.println("statement:  " + state.toString());
			if (state.getKind() == Statement.Kind.NORMAL)
				System.out.println("----------" + i + ((NormalStatement) state).getInstruction().toString());
//			if (state.getKind() == Statement.Kind.PARAM_CALLEE)
//				System.out.println("----------" + i + ((ParamCallee) state).getValueNumber());
			System.out.println();
			// System.err.println(n);

		}
	}

	// 我们需要知道，seed在哪个函数中，所以，我们要先在cg图里找到哪个节点是对应的函数，
	// cg图，函数名，函数所在的类
	public static CGNode findMethod(CallGraph cg, String Name, String methodCLass) {
		if (Name.equals(null) && methodCLass.equals(null))
			return null; // 构建 atom（wala中），用来比较CGNode的名字和类
		Atom name = Atom.findOrCreateUnicodeAtom(Name); // 迭代cg图
		int i = 1;
		for (Iterator<? extends CGNode> it = cg.iterator(); it.hasNext();) {

			CGNode n = it.next(); // 函数名和类名比较
			// System.out.println(n.getMethod().getName()+"sss"+n.getMethod().getDeclaringClass().getName().toString());
			// System.out.println(name+"dddd"+methodCLass);
			if (n.getMethod().getName().equals(name)
					&& n.getMethod().getDeclaringClass().getName().toString().equals(methodCLass)) {

				return n;
			}
		}
		Assertions.UNREACHABLE("Failed to find method " + name);
		return null;
	}

	// 在CGNode中找到seed
	// n是第二步的返回值，methodName是seed的函数名
	public static Statement findCallTo(CGNode n, String methodName) {
		// 函数n的ir，中间表示（和llvm的ir很相似）。
		IR ir = n.getIR();
		// System.out.println(ir.toString());
		// 迭代ir中每一条指令，寻找seed
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next(); // 当前指令是调用指令，比较函数名，是否是seed；
			if (s instanceof SSAInvokeInstruction) {
				SSAInvokeInstruction call = (SSAInvokeInstruction) s;
				if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
					IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
					Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
					return new NormalStatement(n, indices.intIterator().next());
				}
			}
		}
		Assertions.UNREACHABLE("Failed to find call to " + methodName + " in " + n);
		return null;
	}

	// 打印切片信息
	public static void printState(Statement s) {
		// 直接输出该statement
		// System.out.println("cgnode:" + s);
		if (s.getKind() == Statement.Kind.NORMAL
				&& !(s.getNode().getMethod().getName().equals(Atom.findOrCreateAsciiAtom("fakeRootMethod")))) {
			CGNode node = s.getNode();

			// 对应的method：
			IMethod method = node.getMethod();

			// 对应的class：
			IClass klass = s.getNode().getMethod().getDeclaringClass();

			// 输出类和方法
			System.out.println("method:" + method.getName() + ", class:" + klass.getName());

			// 输出行
			if (s.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of statements
				int bcIndex, instructionIndex = ((NormalStatement) s).getInstructionIndex();
				try {
					bcIndex = ((ShrikeBTMethod) s.getNode().getMethod()).getBytecodeIndex(instructionIndex);
					try {
						int src_line_number = s.getNode().getMethod().getLineNumber(bcIndex);
						System.out.println("Source line number = " + src_line_number);
					} catch (Exception e) {
						System.err.println("Bytecode index no good");
						System.err.println(e.getMessage());
					}
				} catch (Exception e) {
					System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
					System.err.println(e.getMessage());
				}
			}
		}

	}

	// 通过调用图节点获取某个方法所在行
	public static int getMethodLine(CGNode n) {
		// 函数n的ir，中间表示（和llvm的ir很相似）。
		IR ir = n.getIR();
		IBytecodeMethod<?> method = (IBytecodeMethod<?>) ir.getMethod();
		int bytecodeIndex = 0;
		try {
			bytecodeIndex = method.getBytecodeIndex(1);
		} catch (InvalidClassFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int sourceLineNum = method.getLineNumber(bytecodeIndex);
		return sourceLineNum;
	}

	// 通过cgnode信息找到目标statement，未实现通用方法
	public static Statement getStatement(Statement state, String method, int value, String kind) {
		if (state.getKind() == Statement.Kind.NORMAL && kind.equals("return")
				&& state.getNode().getMethod().getName().toString().equals(method)) {
			if (((NormalStatement) state).getInstruction().toString().contains("return 4")) {
				return state;
			}
			;
		}
		if (state.getKind() == Statement.Kind.NORMAL && kind.equals("line")
				&& state.getNode().getMethod().getName().toString().equals(method)) {
			if (((NormalStatement) state).getInstruction().toString().contains("putfield 1.")) {
				return state;
			}
			;
		}
		if (state.getKind() == Statement.Kind.PARAM_CALLEE && kind.equals("call")) {
			if (((ParamCallee) state).getValueNumber() == value
					&& state.getNode().getMethod().getName().toString().equals(method)) {
				return state;
			}
			;
		}
		if (state.getKind() == Statement.Kind.PARAM_CALLER && kind.equals("caller")) {
			if (state.getNode().getMethod().getName().toString().equals(method)) {
				return state;
			}
			;
		}
//		if (state.getKind() == Statement.Kind.NORMAL_RET_CALLEE&& kind.equals("retcall")) {
//			// System.out.println(((NormalStatement) state).getInstruction().toString()+"
//			// "+state.getNode().getMethod().getName().toString());
//			if (state.getNode().getMethod().getName().toString().contains("getMin")) {
//				return state;
//			}
//			;
//		}
		return null;
	}

	// 构建入口点
	public static Iterable<Entrypoint> makeEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
		if (scope == null) {
			throw new IllegalArgumentException("scope is null");
		}
		return makeEntrypoints(scope.getApplicationLoader(), cha);
	}

	public static Iterable<Entrypoint> makeEntrypoints(ClassLoaderReference clr, IClassHierarchy cha) {
		if (cha == null) {
			throw new IllegalArgumentException("cha is null");
		}
		final Atom mainMethod = Atom.findOrCreateAsciiAtom("main");//
		// System.out.println(mainMethod + "============");
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for (IClass klass : cha) {
			if (klass.getClassLoader().getReference().equals(clr)) {
				MethodReference mainRef = MethodReference.findOrCreate(klass.getReference(), mainMethod,
						Descriptor.findOrCreateUTF8("([Ljava/lang/String;)V"));
				System.out.println(mainRef + "============");
				IMethod m = klass.getMethod(mainRef.getSelector());

				if (m != null) {
					result.add(new DefaultEntrypoint(m, cha));
				}
			}
		}
		return result::iterator;
	}
	public static Iterable<Entrypoint> makeEntrypoints(Iterable<Entrypoint> entrypoints) {
		final HashSet<Entrypoint> result = HashSetFactory.make();

		for (Entrypoint E : entrypoints) {
			if (E.getMethod().getName().toString().equals("linkBefore")) {
				System.out.println("Entrypoint: " + E);
				result.add(E);
			}
		}
		return result::iterator;
	}

	public static void main(String args[]) throws IOException, ClassHierarchyException, IllegalArgumentException,
			InvalidClassFileException, CancelException {
		// 获得一个文件
		File exFile = new FileProvider().getFile("Java60RegressionExclusions.txt");
		// 将分析域存到文件中
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("D:\\work\\Java\\mix\\list", exFile);// 分析的路径，文件夹或jar包
		// 构建ClassHierarchy，相当与类的一个层级结构
		ClassHierarchy cha = ClassHierarchyFactory.make(scope); // 循环遍历每一个类

		// 构建入口点
		// Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha);//
		// Util类中以main方法为入口点
		Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);
		Iterable<Entrypoint> entrypoints1=makeEntrypoints(entrypoints);
		// 在本类中自己定义的以某个方法为入口点
		// Iterable<Entrypoint> entrypoints = makeEntrypoints(scope, cha);
		// Entrypoint test
		// System.out.println(scope.getApplicationLoader().getName());

//		AnalysisOptions options = new AnalysisOptions();
//		Iterable<? extends Entrypoint> entrypoints = DalvikCallGraphTestBase.getEntrypoints(cha);
		// options.setEntrypoints(entrypoints);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		options.setReflectionOptions(ReflectionOptions.NONE);
		AnalysisOptions options1 = new AnalysisOptions(scope, entrypoints1);
		
//		for (Entrypoint E : options.getEntrypoints()) {
//
//			if (E.getMethod().getName().toString().equals("linkFirst")) {
//				System.out.println("Entrypoint: " + E);
//				options1 = new AnalysisOptions(scope, (Iterable<? extends Entrypoint>) E);
//			}
//		}
		// build call graph
		CallGraphBuilder<InstanceKey> builder = Util.makeVanillaZeroOneCFABuilder(Language.JAVA, options,
				new AnalysisCacheImpl(), cha, scope);
		CallGraph cg = builder.makeCallGraph(options1, null);
		// Build sdg
		ModRef<InstanceKey> modRef = ModRef.make();
		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
		SDG<?> sdg = new SDG<>(cg, pa, modRef, DataDependenceOptions.FULL , ControlDependenceOptions.NO_INTERPROC_NO_EXCEPTION, null);

		// 打印CG和SDG
		// System.out.println("------------------------------cg------------------------");
		//print(cg);
		// System.out.println("-------------------------");
		 printSDG(sdg, "linkBefore");
		// System.out.println("------------------------------sdg------------------------");
		 //printSDG(sdg);
		// System.out.println(getMethodLine(findMethod(cg, "getMin", "Lpaixu/w")));

		// find the seed statement. The seed statement in this example is the first
		// instance of calling a indexOf() function in the contains function.
		// CGNode n = findMethod(cg, "contains", "Llist/LinkedList");
		// Statement state = findCallTo(n, "indexOf");
		// 自动导入包即可，slicer还有很多方法，如反向切片，上下文敏感等\
		Statement state = null;
		Collection<Statement> slice = null;
		for (Iterator<Statement> it = sdg.iterator(); it.hasNext();) {
			state = it.next();
			String kind = "call";// 函数名和类名比较
			state = getStatement(state, "linkBefore", 3, kind);
			if (state != null) {
				System.err.println("Statement: " + state);
				if (kind.equals("call")||kind.equals("caller")) {
					slice = Slicer.computeForwardSlice(sdg, state);
				} else {
					ThinSlicer ts = new ThinSlicer(cg, pa);
					slice = ts.computeBackwardThinSlice(state);
					slice = Slicer.computeBackwardSlice(sdg, state);
				}

				break;
			}
		}
//
//		// 普通slicer
//		// Collection<Statement> slice = Slicer.computeForwardSlice(sdg, state);
//		// 如果要使用thin Slicer
//		// ThinSlicer ts = new ThinSlicer(cg, pa);
//		// Collection<Statement> slice = ts.computeBackwardThinSlice(state);
//
//		// 遍历切片产生的statement
		System.out.println();
		for (Statement s : slice) {
			if (s.getKind() == Statement.Kind.NORMAL) {
				// 输出信息
				printState(s);
			}

		}

		// 循环输出类名和方法名
		// 输出实例：
		/*
		 * out: Lpaixu/w <init> bubbleSort getMiddle quickSort quick <init>
		 * registerNatives getClass hashCode equals clone toString notify notifyAll wait
		 * wait wait finalize <clinit>
		 */
//		for (IClass klass : cha) { // 打印类名
//			 //
//			// System.out.println("classssssssss");
//			// 判断当前类是否在zookeeper中
//			if (scope.isApplicationLoader(klass.getClassLoader())) {
//				// // 对在zookeeper中的类的每个函数遍历，并打印函数名
//				System.out.println(klass.getName().toString());
//				for (IMethod m : klass.getAllMethods()) {
//					 System.out.println(m.getName().toString());
//				}
//			} else {
//				//System.out.println("ssss");
//			}
//		}

	}
}
