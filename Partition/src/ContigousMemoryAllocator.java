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

	// System.out.printlns the list of available commands
	public void print_help_message() {
		// TODO: add code below
		System.out.println("RQ <process> <size> to request memory of size for the process");
		System.out.println("RL <process> to release the memory of the process");
		System.out.println("STAT to show the memory allocation status");
		System.out.println("EXIT to exit");
		System.out.println("HELP to show the availiable commands");
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
	private static boolean help = false; 
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
	
	public static void main(String[] args) {
		int MEMORY_MAX = 1024;
		int PROC_SIZE_MAX = 256;
		int NUM_PROC = 10;
		int MAX_PROC_TIME = 1000;
		Scanner scanner;
		boolean flag = true;
		String[] dataLine;
		String data;
		int algorithim;

		while (flag != false) {
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
				case 1:
					// Enter demo file
					try {
						System.out.println("Type file's directory");
						scanner = new Scanner(System.in); // Create a Scanner object
						String fileDir = scanner.nextLine();
						File myObj = new File(fileDir);
						Scanner myReader = new Scanner(myObj);
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

				case 2:
					// Create Processes
					Random rand = new Random();
					// List<Integer> processInfo = new ArrayList<Integer> ();
					List<String> processID = new ArrayList<String>();
					List<Integer> processSize = new ArrayList<Integer>();
					List<Integer> processTime = new ArrayList<Integer>();
					List<Integer> processTimeSave = new ArrayList<Integer>();
					List<Integer> processStatus = new ArrayList<Integer>();
					List<Integer> processReset = new ArrayList<Integer>();
					// Dictionary<String, List<Integer>> dict= new Hashtable<>();
					ContigousMemoryAllocator memory = new ContigousMemoryAllocator(MEMORY_MAX);
					String processString = "";
					// Processes
					for (int i = 1; i <= NUM_PROC; i++) {
						processSize.add(rand.nextInt(PROC_SIZE_MAX));
						processTime.add(rand.nextInt(MAX_PROC_TIME));
						processStatus.add(0);
						processString = "P" + i;
						processID.add(processString);
						processTimeSave.add(processTime.get(i - 1));
						processReset.add(processStatus.get(i - 1));

					}
					
					System.out.println("\nProcesses created " + processID);
					boolean runFlag = true;

					Signal.handle(new Signal("INT"), new SignalHandler() {
						public void handle(Signal sig) {
							help = true;
						}
					});

					while (runFlag != false) {
						// System.out.println(memory.free_memory());

						// Which algorithm to run the process on
						System.out.println("\nSelect an algorithm:");
						System.out.println("First Fit: 0");
						System.out.println("Best Fit: 1");
						System.out.println("Worst Fit: 2");
						scanner = new Scanner(System.in);
						algorithim = scanner.nextInt();
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

						boolean memoryFlag = true;
						// boolean runFlag = true;
						int check = 0;
						int doneCount = 0;

						while (memoryFlag != false) {
							
							print_run(memory, MEMORY_MAX, processSize, processID, processTime, processStatus);

							for (int i = 0; i < processID.size(); i++) {

								// Add to memory
								// Process info index 0 Size index 1 Time left 2 a three integer switch where 0
								// is not in memory but not done 1 is in memory and running 2 is finished
								//
								if (processStatus.get(i) == 0 && processTime.get(i) != 0) {
									// TODO: Get output from first fit to check if space or not then print error if
									// so

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
									
									if (check > 0) {
										processStatus.set(i, 1);

									} else {
										System.out.println(processID.get(i) + " was not able to be fitted with size "
												+ processSize.get(i) + " KB");
										// set the process as running in memory in the array

									}
								}
								// Release
								else if (processStatus.get(i) == 1 && processTime.get(i) == 0) {
									// check if alloc in some way then check if process has time left if not then
									// release
									check = memory.release(processID.get(i));
									// set process as done so we dont run through it again
									if (check == -1) {
										System.out.println("Failed Search");
									} else {
										processStatus.set(i, 2);
										doneCount++;
									}

								}
								// Tick process thats in memory down one
								else if (processStatus.get(i) == 1 && processTime.get(i) != 0) {
									// System.out.println("Ticked Time Down");
									// remove 1000 ms or 1 sec from the time on the process
									processTime.set(i, processTime.get(i) - 1);
								}
								
							}
							if (doneCount == processID.size()) {
								memory.merge_holes();
								print_run(memory, MEMORY_MAX, processSize, processID, processTime, processStatus);

								System.out
										.println("Input Rerun to run on another fit algorithim with the same values else enter anything: ");
								scanner = new Scanner(System.in);
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
							
								// should be 1 millis to be accuracte but that is just hard on the eyes and
								// makes it a hard to watch
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(help == true){
								String pauseMenu = "";
								System.out.println("\nEnter Resume to resume");
								System.out.println("Enter Exit to exit");
								scanner = new Scanner(System.in);
								pauseMenu = scanner.nextLine();
								if (pauseMenu.equalsIgnoreCase("Resume") == true) {
									help = false;
								} else if (pauseMenu.equalsIgnoreCase("Exit") == true) {
									runFlag = false;
									memoryFlag = false;
								}
							}
						}
					}
					break;

				case 3:
					flag = false;
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
