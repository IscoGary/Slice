package test;

public class sort {
	/**
	 * ð������ �Ƚ����ڵ�Ԫ�ء������һ���ȵڶ����󣬾ͽ�������������
	 * ��ÿһ������Ԫ����ͬ���Ĺ������ӿ�ʼ��һ�Ե���β�����һ�ԡ�����һ�㣬����Ԫ��Ӧ�û����������� ������е�Ԫ���ظ����ϵĲ��裬�������һ����
	 * ����ÿ�ζ�Խ��Խ�ٵ�Ԫ���ظ�����Ĳ��裬ֱ��û���κ�һ��������Ҫ�Ƚϡ�
	 * 
	 * @param numbers ��Ҫ�������������
	 */
	public static void bubbleSort(int[] numbers) {
		int temp = 0;
		int size = numbers.length;
		for (int i = 0; i < size - 1; i++) {
			for (int j = 0; j < size - 1 - i; j++) {
				if (numbers[j] > numbers[j + 1]) // ��������λ��
				{
					temp = numbers[j];
					numbers[j] = numbers[j + 1];
					numbers[j + 1] = temp;
				}
			}
		}
	}

	/**
	 * ���ҳ����ᣨĬ�������λlow������numbers�������������λ��
	 * 
	 * @param numbers ����������
	 * @param low     ��ʼλ��
	 * @param high    ����λ��
	 * @return ��������λ��
	 */
	public static int getMiddle(int[] numbers, int low, int high) {
		int temp = numbers[low]; // ����ĵ�һ����Ϊ����
		while (low < high) {
			while (low < high && numbers[high] > temp) {
				high--;
			}
			numbers[low] = numbers[high];// ������С�ļ�¼�Ƶ��Ͷ�
			while (low < high && numbers[low] < temp) {
				low++;
			}
			numbers[high] = numbers[low]; // �������ļ�¼�Ƶ��߶�
		}
		numbers[low] = temp; // �����¼��β
		return low; // ���������λ��
	}

	/**
	 * 
	 * @param numbers ����������
	 * @param low     ��ʼλ��
	 * @param high    ����λ��
	 */
	public static void quickSort(int[] numbers, int low, int high) {
		if (low < high) {
			int middle = getMiddle(numbers, low, high); // ��numbers�������һ��Ϊ��
			quickSort(numbers, low, middle - 1); // �Ե��ֶα���еݹ�����
			quickSort(numbers, middle + 1, high); // �Ը��ֶα���еݹ�����
		}

	}

	/**
	 * ��������
	 * 
	 * @param numbers ����������
	 */
	public static void quick(int[] numbers) {
		if (numbers.length > 0) // �鿴�����Ƿ�Ϊ��
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
