package Elevators;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ElevatorSystem {
    public static final int ELEVATOR_SPEED = 2000;
    public static final int BETWEEN_CALL_TIME = 6000;
    public static final int NUMBER_OF_FLOORS = 8;

    public static class Elevator {
        private final int id;
        private int currentFloor;
        private final Queue<Integer> requests;
        private final Object lock;
        private volatile boolean running;

        public Elevator(int id) {
            this.id = id;
            this.currentFloor = 0; // Assume ground floor is 0
            this.requests = new LinkedList<>();
            this.lock = new Object();
            this.running = true;
        }

        public void addRequest(int floor) {
            synchronized (lock) {
                requests.add(floor);
                System.out.println("Elevator " + id + " received request for floor " + floor);
                lock.notify();
            }
        }

        public void move() {
            while (running) {
                Integer nextFloor;
                synchronized (lock) {
                    while (requests.isEmpty() && running) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (!running) break;
                    nextFloor = requests.poll();
                }

                while (currentFloor != nextFloor && running) {
                    if (currentFloor < nextFloor) {
                        currentFloor++;
                    } else {
                        currentFloor--;
                    }
                    System.out.println("Elevator " + id + " moving to floor " + currentFloor);
                    try {
                        Thread.sleep(ELEVATOR_SPEED); // Simulate time taken to move
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (running) {
                    System.out.println("Elevator " + id + " arrived at floor " + currentFloor);
                }
            }
        }

        public int getCurrentFloor() {
            return currentFloor;
        }

        public int getId() {
            return id;
        }

        public void stop() {
            running = false;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }


    public static class ElevatorManager {
        private final List<Elevator> elevators;

        public ElevatorManager() {
            elevators = new ArrayList<>();
            elevators.add(new Elevator(1));
            elevators.add(new Elevator(2));

            for (Elevator elevator : elevators) {
                new Thread(elevator::move).start();
            }
        }

        public synchronized void requestElevator(int floor) {
            Elevator bestElevator = null;
            int minDistance = Integer.MAX_VALUE;

            for (Elevator elevator : elevators) {
                int distance = Math.abs(elevator.getCurrentFloor() - floor);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestElevator = elevator;
                }
            }

            if (bestElevator != null) {
                bestElevator.addRequest(floor);
            }
        }

        public void stopAllElevators() {
            for (Elevator elevator : elevators) {
                elevator.stop();
            }
        }
    }


    public static class RequestGenerator implements Runnable {
        private final ElevatorManager manager;
        private volatile boolean running;

        public RequestGenerator(ElevatorManager manager) {
            this.manager = manager;
            this.running = true;
        }

        @Override
        public void run() {
            Random random = new Random();
            while (running) {
                int floor = random.nextInt(NUMBER_OF_FLOORS);
                manager.requestElevator(floor);
                try {
                    Thread.sleep(random.nextInt(BETWEEN_CALL_TIME));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void stop() {
            running = false;
        }
    }
}