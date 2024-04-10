import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class ContigousMemoryAllocator {
	private int size; // maximum memory size in bytes (B)
	private Map<String, Partition> allocMap; // map process to partition
	private List<Partition> partList; // list of memory partitions
	// constructor

	public ContigousMemoryAllocator(int size) {
		this.size = size;
		this.allocMap = new HashMap<>();
		this.partList = new ArrayList<>();
		this.partList.add(new Partition(0, size)); // add the first hole, which is the whole memory at start up
	}


	// System.out.printlns the allocation map (free + allocated) in ascending order
	// of base addresses
	public void print_status() {
		// TODO: add code below
		order_partitions();
		System.out.printf("Partitions [Allocated = %d KB, Free = %d KB]:\n", allocated_memory(), free_memory());
		for (Partition part : partList) {
			System.out.printf("Address [%d:%d] status (%s KB)\n",
					part.getBase(), part.getBase() + part.getLength() - 1,
					part.isFreeOrNot() ? "Free" : part.getProcess());
		}
	}

	// get the size of total allocated memory
	private int allocated_memory() {
		// TODO: add code below
		int allocated = 0;
		for (Partition part : partList) {
			if (!part.isFreeOrNot()) {
				allocated += part.getLength();

			}
		}
		return allocated;
	}

	// get the size of total free memory
	private int free_memory() {
		// TODO: add code below
		int free = 0;
		for (Partition part : partList) {
			if (part.isFreeOrNot()) {
				free += part.getLength();

			}
		}
		return free;

	}

	// get the num of holes
	private int num_holes() {
		// TODO: add code belows
		int holes = 0;
		for (Partition part : partList) {
			if (part.isFreeOrNot()) {
				holes += 1;

			}
		}
		return holes;

	}

	// sort the list of partitions in ascending order of base addresses
	private void order_partitions() {
		// TODO: add code below
		Collections.sort(partList, Comparator.comparingInt(Partition::getBase));
	}

	// implements the first fit memory allocation algorithm
	public int first_fit(String process, int size) {
		// TODO: add code below

		int index = 0;
		int alloc = -1;
		while (index < partList.size()) {
			if (allocMap.containsKey(process)) {
				return -1;
			}
			Partition part = partList.get(index);
			if (part.isFreeOrNot() && part.getLength() >= size) {
				Partition newPart = new Partition(part.getBase(), size);
				newPart.setFreeOrNot(false);
				newPart.setProcess(process);
				partList.add(index, newPart);
				allocMap.put(process, newPart);
				part.setBase(part.getBase() + size);
				part.setLength(part.getLength() - size);
				if (part.getLength() == 0) {
					partList.remove(part);
				}
				alloc = size;
				break;
			} else {
				index++;
			}

		}
		return alloc;
	}

	// Worst fit
	public int worst_fit(String process, int size) {
		// TODO: add code below
		Partition worstFit = null;
		int worstSize = 0;
		if (allocMap.containsKey(process)) {
			return -1;
		}
		int index = 0;
		int alloc = -1;
		while (index < partList.size()) {
			Partition part = partList.get(index);
			if (part.isFreeOrNot() && part.getLength() >= size && part.getLength() < worstSize) {
				worstFit = part;
				worstSize = part.getLength();
			}
		}
		if (worstFit != null) {
			Partition newPart = new Partition(worstFit.getBase(), size);
			newPart.setFreeOrNot(false);
			newPart.setProcess(process);
			partList.add(index, newPart);
			allocMap.put(process, newPart);
			worstFit.setBase(worstFit.getBase() + size);
			worstFit.setLength(worstFit.getLength() - size);
			if (worstFit.getLength() == 0) {
				partList.remove(worstFit);
				return size;
				// alloc = size;
				// break;
			}
		}
		return alloc;
	}

	// release the allocated memory of a process
	public int release(String process) {
		// TODO: add code below
		int free = -1;
		for (Partition part : partList) {
			if (!part.isFreeOrNot() && part.getProcess().equals(process)) {
				part.setFreeOrNot(true);
				part.setProcess(null);
				free = part.getLength();
				break;
			}

		}
		if (free < 0) {
			return free; // failed search
		}
		// Merge stuffs like the adjacent holes

		merge_holes();
		return free;
	}

	// procedure to merge adjacent holes
	private void merge_holes() {
		// TODO: add code below

		order_partitions();
		int i = 0;

		while (i < partList.size()) {
			Partition part = partList.get(i);
			if (!part.isFreeOrNot()) {
				i++;
				continue;
			}

			int end_i = part.getBase() + part.getLength() - 1;
			int j = i + 1;
			while (j < partList.size() && partList.get(j).isFreeOrNot()) {

				// merge j into i
				int start_j = partList.get(j).getBase();
				if (start_j == end_i + 1) {
					part.setLength(part.getLength() + partList.get(j).getLength());
					partList.remove(j);
				} else {
					break;
				} // end second while loop

			}

			i++;
		} // end first while loop

	}
	/* """
	Prints all the info during memory allocation

    :param memory: instance of memory
    :type: ContigousMemoryAllocator
	:param MEMORY_MAX: Max amount of memory in the instance of memory
    :type: int
	:param processSize: list of all the process's sizes
    :type: list of integers
	:param processID: list of all the process's IDs
    :type: list of integers
	:param processTime: list of all the process's time to run
    :type: list of integers
	:param processStatus: list of all the processes status in the memory at that moment
    :type: list of integers
    :return: prints the memory at that moment and all relevent info
    :rtype: none
    """ */
	public static void print_run(ContigousMemoryAllocator memory, int MEMORY_MAX, List<Integer> processSize,
			List<String> processID, List<Integer> processTime, List<Integer> processStatus) {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		System.out.println("--------------------------------------------------");
		memory.print_status();
		System.out.println("--------------------------------------------------");
		System.out.println("ID: " + processID);
		System.out.println("Time: " + processTime);
		System.out.println("Size: " + processSize);
		System.out.println("Status: " + processStatus);
		System.out
				.println("Status: 0 process is not allco to memory yet, 1 process is in memory, 2 process is finished");
		System.out.println("--------------------------------------------------");
		System.out.println("# of Holes: " + memory.num_holes());
		System.out.println("Average Size of Holes: " + (memory.free_memory() / memory.num_holes()) + " KB");
		System.out.println("Total Size of Holes: " + memory.free_memory() + " KB");
		System.out.println("Percentage of Free Memory in the Memory: "
				+ (Math.round((memory.free_memory() / (MEMORY_MAX * 1.0)) * 100)) + "%");
		System.out.println("--------------------------------------------------");
		System.out.println("Control C to pause the simulation");
		System.out.println("--------------------------------------------------");
	}
	//Flag for when control C is pressed to pause the program
	private static boolean pause = false; 
	public static void main(String[] args) {
		//Base whole memory size
		int MEMORY_MAX = 1024;
		//Max size of processes
		int PROC_SIZE_MAX = 256;
		//Number of processes
		int NUM_PROC = 10;
		//Max time of process
		int MAX_PROC_TIME = 1000;
		Scanner scanner = new Scanner(System.in); // Create a Scanner object
		//Flag for the main menu to run
		boolean mainMenuFlag = true;

		//Main Menu Loop
		while (mainMenuFlag != false) {
			System.out.println("--------------------------------------------------");
			System.out.println("Select an option");
			System.out.println("1. Input a File");
			System.out.println("2. Run Processes");
			System.out.println("3. Quit");
			// System.out.println("4. Save Output");
			System.out.println("--------------------------------------------------");
			scanner = new Scanner(System.in); // Create a Scanner object
			int input = scanner.nextInt();
			// Switch Case to pick which option from menu
			switch (input) {
				
					/* """
					Case 1: Reads a file given and sets the parameters for running the memory and creating process
					from the ones given in the file

					:param data: line of the file
					:type: String
					:param dataLine: array of the split words in the line taken from the file
					:type: array
					:param fileDir: String holding the directory given to read from
					:type: String
					:param file: file Object given from the directory entered
					:type: File
					:return: Updates all the memory and process values such as memory max with the values from the file given
					:rtype: int
					""" */
				case 1:
					String[] dataLine;
					String data;
					// Enter demo file
					try {
						System.out.println("Type file's directory");
						String fileDir = scanner.nextLine();
						File file = new File(fileDir);
						Scanner myReader = new Scanner(file);
						dataLine = null;
						data = "";
						while (myReader.hasNextLine()) {
							data = myReader.nextLine();
							dataLine = data.split(" ");
							if (dataLine[0].equalsIgnoreCase("MEMORY_MAX") == true) {
								MEMORY_MAX = Integer.valueOf(dataLine[2]);
							} else if (dataLine[0].equalsIgnoreCase("PROC_SIZE_MAX") == true) {
								PROC_SIZE_MAX = Integer.valueOf(dataLine[2]);
							} else if (dataLine[0].equalsIgnoreCase("NUM_PROC") == true) {
								NUM_PROC = Integer.valueOf(dataLine[2]);
							} else if (dataLine[0].equalsIgnoreCase("MAX_PROC_TIME") == true) {
								MAX_PROC_TIME = Integer.valueOf(dataLine[2]);
							}
						}
						myReader.close();
					} catch (FileNotFoundException e) {
						System.out.println("File not Found");
						e.printStackTrace();
					}
					break;


					/* """
					Case 2: Creates the Processes and their related sizes and times. Then runs the memory allocotor using the algorithm given by the user.
					If the user inputs Control C then the program pauses and waits for input to resume or exit back to menu

					:return: Outputs to terminal the simulation of memory allocation
					:rtype: none
					""" */
				case 2:

					// Create Processes
					//Would be its own method however java doesnt allow multiple return methods and the work arounds arent worth the work

					Random rand = new Random();
				
					//Arrays holding the info of all the processes
					List<String> processID = new ArrayList<String>();
					List<Integer> processSize = new ArrayList<Integer>();
					List<Integer> processTime = new ArrayList<Integer>();
					// Process status index 0 Size index 1 Time left 2 a three integer switch where 0
					// is not in memory but not done 1 is in memory and running 2 is finished
					List<Integer> processStatus = new ArrayList<Integer>();
					//Arrays to save the instance of processTime and processStatus so the simulation can be rerun on the same processes
					List<Integer> processTimeSave = new ArrayList<Integer>();
					List<Integer> processReset = new ArrayList<Integer>();
					//Instance of memory
					ContigousMemoryAllocator memory = new ContigousMemoryAllocator(MEMORY_MAX);
					//String holder the processID to be added to processID
					String processString = "";

					//For the number of proceses created a process and save the data in arrays of intergers
					for (int i = 1; i <= NUM_PROC; i++) {
						processSize.add(rand.nextInt(PROC_SIZE_MAX));
						processTime.add(rand.nextInt(MAX_PROC_TIME));
						processStatus.add(0);
						processString = "P" + i;
						processID.add(processString);
						//Add the info from the lists so that if the user chooses to rerun the program the info will be saved and can be loaded
						processTimeSave.add(processTime.get(i - 1));
						processReset.add(processStatus.get(i - 1));

					}
					System.out.println("\nProcesses created " + processID);
					


					//Signal Checks for Control C if detected then sets pause to true to pause the program
					Signal.handle(new Signal("INT"), new SignalHandler() {
						public void handle(Signal sig) {
							//set static variable to true
							pause = true;
						}
					});

					//Outer While Loop this is so if the user chooses they can rerun on another anlogrithm if they so choose
					boolean runFlag = true;
					while (runFlag != false) {
						// System.out.println(memory.free_memory());

						// Which algorithm to run the process on
						System.out.println("\nSelect an algorithm:");
						System.out.println("First Fit: 0");
						System.out.println("Best Fit: 1");
						System.out.println("Worst Fit: 2");
						//Take the input given for the algorithm
						int algorithim;
						algorithim = scanner.nextInt();
						//Print out the choice to the user choose
						switch (algorithim) {
							case 0:
								System.out.println("First Fit selected");
								break;
							case 1:
								System.out.println("Best Fit selected");
								break;
							case 2:
								System.out.println("Worst Fit selected");
								break;
							// System.out.println("Not Valid Input");
						}
						//Inner while loop flag
						boolean memoryFlag = true;
						//check for the output of the algorthim given to see if there is space
						int check = 0;
						//Count how many processes finished their time in memory
						int doneCount = 0;

						//Inner Loop
						while (memoryFlag != false) {
							//print the state of memory
							print_run(memory, MEMORY_MAX, processSize, processID, processTime, processStatus);
							
							//Run through the processes either alloc them, release or tick down their time
							for (int i = 0; i < processID.size(); i++) {

								// Add to memory if status is 0 and there is time left for the process
								// Process status index 0 Size index 1 Time left 2 a three integer switch where 0
								// is not in memory but not done 1 is in memory and running 2 is finished
								//
								if (processStatus.get(i) == 0 && processTime.get(i) != 0) {
									//Get output from first fit to check if space or not then print error if
									// so
									
									//Pick the algorithim choosen by the user earlier
									switch (algorithim) {
										case 0:
											// Firstfit
											check = memory.first_fit(processID.get(i), processSize.get(i));
											break;
										case 1:
											// BestFit
											break;
										case 2:
											// WorstFit
											break;
										// System.out.println("Not Valid Input");
									}
									//Get output from first fit to check if space or not then print error
									if (check > 0) {
										processStatus.set(i, 1);

									} else {
										//Print error that theres no room for the process
										System.out.println(processID.get(i) + " was not able to be fitted with size "
												+ processSize.get(i) + " KB");
										// set the process as running in memory in the array

									}
								}
								// Release the process if the process status is 1 or in memory and theres no time left for the process 
								else if (processStatus.get(i) == 1 && processTime.get(i) == 0) {

									check = memory.release(processID.get(i));
									//If process wasnt found print the error
									if (check == -1) {
										System.out.println("Failed Search");
									} else {
										//If found then update the process in the array
										// set process as done so we dont run through it again
										processStatus.set(i, 2);
										//Increment processes done
										doneCount++;
									}

								}
								// Tick processes thats in memory time down 1 ms if the status is 1 or in memory and there is time left for the process in memory
								else if (processStatus.get(i) == 1 && processTime.get(i) != 0) {
									// System.out.println("Ticked Time Down");
									// remove 1000 ms or 1 sec from the time on the process
									processTime.set(i, processTime.get(i) - 1);
								}
								
							}
							//If amount of processes out of memory = the amount of processes print the final instance of memory after merging the last holes
							if (doneCount == processID.size()) {
								memory.merge_holes();
								print_run(memory, MEMORY_MAX, processSize, processID, processTime, processStatus);

								//Ask if the user wasnts to rerun on the same processes on another algorthim or exit
								System.out
										.println("Input Rerun to run on another fit algorithim with the same values else enter anything: ");
								String rerun = "";
								rerun = scanner.nextLine();
								if (rerun.equalsIgnoreCase("Rerun") == true) {
									runFlag = true;
									memoryFlag = false;
									processTime = processTimeSave;
									processStatus = processReset;
									doneCount = 0;
									algorithim = -1;
									memory = new ContigousMemoryAllocator(MEMORY_MAX);
								} else {
									runFlag = false;
									memoryFlag = false;
								}

							}
							
								// should be 1 millis to be accurate but that is just too hard on the eyes 
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								
								e.printStackTrace();
							}
							// If control C is detected then pause the while loop 
							if(pause == true){
								String pauseMenu = "";
								//Ask the user to input resume or exit
								System.out.println("\nEnter Resume to resume");
								System.out.println("Enter Exit to exit");
								pauseMenu = scanner.nextLine();
								//If the input is resume resume the memory program
								if (pauseMenu.equalsIgnoreCase("Resume") == true) {
									pause = false;
									//If input is exit then return to menu
								} else if (pauseMenu.equalsIgnoreCase("Exit") == true) {
									runFlag = false;
									memoryFlag = false;
								}
							}
						}
					}
					break;
				//Exit whole program case
				case 3:
					mainMenuFlag = false;
					break;

				/*
				 * case 4:
				 * //Save the output
				 * saveFiles(file, log)
				 * case 5:
				 * flag = false;
				 */
			}
		}
	}
}
