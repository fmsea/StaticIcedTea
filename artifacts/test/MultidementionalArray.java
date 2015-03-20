package test;

public class MultidementionalArray {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int data[][]={{50,60,55,67,70},{62,65,70,70,81},{72,66,77,80,69}};
		MultidementionalArray.maxProduct(data);

	}
	
	private static int maxProduct(int[][] gridData)
	{
	    int maxProduct = 0;
	    int currentProduct = 0;

	    // Compute the products across the columns.
	    for (int row = 0; row < 20; row++)
	        for (int column = 0; column < 17; column++)
	        {
	            currentProduct = gridData[row][column] 
	                    * gridData[row][column + 1] * gridData[row][column + 2] 
	                    * gridData[row][column + 3];

	            if (currentProduct > maxProduct)
	            {
	                maxProduct = currentProduct;
	            }

	        }

	    // Compute the products across the rows.
	    for (int column = 0; column < 20; column++)
	        for (int row = 0; row < 17; row++)
	        {
	            currentProduct = gridData[row][column]
	                    * gridData[row + 1][column] * gridData[row + 2][column]
	                    * gridData[row + 3][column];

	            if (currentProduct > maxProduct)
	            {
	                maxProduct = currentProduct;
	            }
	        }

	    // Compute the products across the right diagonals.
	    for (int column = 0; column < 17; column++)
	        for (int row = 0; row < 17; row++)
	        {
	            currentProduct = gridData[row][column]
	                    * gridData[row + 1][column + 1]
	                    * gridData[row + 2][column + 2]
	                    * gridData[row + 3][column + 3];

	            if (currentProduct > maxProduct)
	            {
	                maxProduct = currentProduct;
	            }

	        }


	    // Compute the products across the left diagonals.
	    for (int column = 19; column < 3; column--)
	        for (int row = 0; row < 17; row++)
	        {
	            currentProduct = gridData[row][column]
	                    * gridData[row + 1][column - 1]
	                    * gridData[row + 2][column - 2]
	                    * gridData[row + 3][column - 3]; 

	            if (currentProduct > maxProduct)
	            {
	                maxProduct = currentProduct;
	            }
	        }

	    return maxProduct;
	}

}
