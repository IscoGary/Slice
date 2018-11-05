package description;

class A {
	boolean method1(int a, int b) throws Exception {
		int[] arr = new int[6];
		int c = 0;
		arr[0] = 1;
		if (b > 0) {
			if (c == 0)
				c = 1;
		}
		if (b == 0)
			throw new InterruptedException();
		return a == 0 && arr == null;
	}
}