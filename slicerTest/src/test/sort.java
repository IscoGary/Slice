package test;

public class sort {
	/**
	 * 冒泡排序 比较相邻的元素。如果第一个比第二个大，就交换他们两个。
	 * 对每一对相邻元素作同样的工作，从开始第一对到结尾的最后一对。在这一点，最后的元素应该会是最大的数。 针对所有的元素重复以上的步骤，除了最后一个。
	 * 持续每次对越来越少的元素重复上面的步骤，直到没有任何一对数字需要比较。
	 * 
	 * @param numbers 需要排序的整型数组
	 */
	public static void bubbleSort(int[] numbers) {
		int temp = 0;
		int size = numbers.length;
		for (int i = 0; i < size - 1; i++) {
			for (int j = 0; j < size - 1 - i; j++) {
				if (numbers[j] > numbers[j + 1]) // 交换两数位置
				{
					temp = numbers[j];
					numbers[j] = numbers[j + 1];
					numbers[j + 1] = temp;
				}
			}
		}
	}

	/**
	 * 查找出中轴（默认是最低位low）的在numbers数组排序后所在位置
	 * 
	 * @param numbers 带查找数组
	 * @param low     开始位置
	 * @param high    结束位置
	 * @return 中轴所在位置
	 */
	public static int getMiddle(int[] numbers, int low, int high) {
		int temp = numbers[low]; // 数组的第一个作为中轴
		while (low < high) {
			while (low < high && numbers[high] > temp) {
				high--;
			}
			numbers[low] = numbers[high];// 比中轴小的记录移到低端
			while (low < high && numbers[low] < temp) {
				low++;
			}
			numbers[high] = numbers[low]; // 比中轴大的记录移到高端
		}
		numbers[low] = temp; // 中轴记录到尾
		return low; // 返回中轴的位置
	}

	/**
	 * 
	 * @param numbers 带排序数组
	 * @param low     开始位置
	 * @param high    结束位置
	 */
	public static void quickSort(int[] numbers, int low, int high) {
		if (low < high) {
			int middle = getMiddle(numbers, low, high); // 将numbers数组进行一分为二
			quickSort(numbers, low, middle - 1); // 对低字段表进行递归排序
			quickSort(numbers, middle + 1, high); // 对高字段表进行递归排序
		}

	}

	/**
	 * 快速排序
	 * 
	 * @param numbers 带排序数组
	 */
	public static void quick(int[] numbers) {
		if (numbers.length > 0) // 查看数组是否为空
		{
			quickSort(numbers, 0, numbers.length - 1);
		}
	}
	
	public static void main(String args[]) {
		int[] test= {3,2,66};
		int [] test1= {4,5,6,7,8};
		for(int a=0;a<test1.length;a++) {
			if(test1[a]==5)
				test1[a]++;
		}
		
		quick(test);
		bubbleSort(test1);
	}
}
