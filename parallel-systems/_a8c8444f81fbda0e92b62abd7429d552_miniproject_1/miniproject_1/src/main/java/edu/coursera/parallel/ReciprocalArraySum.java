package edu.coursera.parallel;

import java.awt.List;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * Class wrapping methods for implementing reciprocal array sum in parallel.
 */
public final class ReciprocalArraySum {

    /**
     * Default constructor.
     */
    private ReciprocalArraySum() {
    }

    /**
     * Sequentially compute the sum of the reciprocal values for a given array.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double seqArraySum(final double[] input) {
        double sum = 0;

        // Compute sum of reciprocals of array elements
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

    /**
     * Computes the size of each chunk, given the number of chunks to create
     * across a given number of elements.
     *
     * @param nChunks The number of chunks to create
     * @param nElements The number of elements to chunk across
     * @return The default chunk size
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Integer ceil
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Computes the inclusive element index that the provided chunk starts at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the start of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The inclusive index that this chunk starts at in the set of
     *         nElements
     */
    private static int getChunkStartInclusive(final int chunk,
            final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Computes the exclusive element index that the provided chunk ends at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the end of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The exclusive end index for this chunk
     */
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    /**
     * This class stub can be filled in to implement the body of each task
     * created to perform reciprocal array sum in parallel.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {
    	
    	static final int SEQUENTIAL_THRESHOLD = 50000;
        /**
         * Starting index for traversal done by this task.
         */
        private final int startIndexInclusive;
        /**
         * Ending index for traversal done by this task.
         */
        private final int endIndexExclusive;
        /**
         * Input array to reciprocal sum.
         */
        private final double[] input;
        /**
         * Intermediate value produced by this task.
         */
        private double value;

        /**
         * Constructor.
         * @param setStartIndexInclusive Set the starting index to begin
         *        parallel traversal at.
         * @param setEndIndexExclusive Set ending index for parallel traversal.
         * @param setInput Input values
         */
        ReciprocalArraySumTask(final int setStartIndexInclusive,
                final int setEndIndexExclusive, final double[] setInput) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
        }

        /**
         * Getter for the value produced by this task.
         * @return Value produced by this task
         */
        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {
            // TODO
        	for(int i =startIndexInclusive; i< endIndexExclusive; i++) {
        		value = value + 1/input[i];
        	}
        	
        }
    }

    /**
     * TODO: Modify this method to compute the same reciprocal sum as
     * seqArraySum, but use two tasks running in parallel under the Java Fork
     * Join framework. You may assume that the length of the input array is
     * evenly divisible by 2.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double parArraySum(final double[] input) {
        assert input.length % 2 == 0;

        int midIndex = input.length/2;
//        double value =0;
//        for(int i=0;i<midIndex;i++) {
//        	value = value + 1/input[i];
//        }
//        
//        for(int i=midIndex;i<input.length;i++) {
//        	value = value + 1/input[i];
//        }
//        
//        return value;
        //bhanuprakash
        //for implementing fork join we need to divide into sub tasks. so normal for loop may not come in handy.
        ReciprocalArraySumTask leftHalfTask = new ReciprocalArraySumTask(0, midIndex , input);
        ReciprocalArraySumTask rightHalfTask = new ReciprocalArraySumTask(midIndex, input.length , input);
        ForkJoinTask.invokeAll(leftHalfTask, rightHalfTask);
        
        return leftHalfTask.getValue() + rightHalfTask.getValue();
    }

    /**
     * TODO: Extend the work you did to implement parArraySum to use a set
     * number of tasks to compute the reciprocal array sum. You may find the
     * above utilities getChunkStartInclusive and getChunkEndExclusive helpful
     * in computing the range of element indices that belong to each chunk.
     *
     * @param input Input array
     * @param numTasks The number of tasks to create
     * @return The sum of the reciprocals of the array input
     */
    protected static double parManyTaskArraySum(final double[] input,
            final int numTasks) {
        double sum = 0;


        ArrayList<ReciprocalArraySumTask> subTasksList = listSubTask(input, numTasks);  
        
        RecursiveAction.invokeAll(subTasksList);
        
        for(ReciprocalArraySumTask subTask : subTasksList)
        {
        	subTask.join();
        	sum += subTask.getValue();
        }
        return sum;
    }
    
    private static ArrayList<ReciprocalArraySumTask> listSubTask(double[] input, int numTasks)
    {
    	  int taskNum = numTasks;
    	  
         if(taskNum>input.length)
         {
             taskNum = input.length;
         }
         
    	ArrayList<ReciprocalArraySumTask> subTaskList = new ArrayList<>();
    	for(int i = 0; i < numTasks; i++)
    	{ 
    		ReciprocalArraySumTask subtask = new ReciprocalArraySumTask(getChunkStartInclusive(i, taskNum, input.length), getChunkEndExclusive(i, taskNum, input.length), input);
    		subTaskList.add(subtask);
    	}
    	return subTaskList;
    }
}
