import mpi.*;

import java.util.Arrays;

public class JavaTasks
{
    public static void main(String[] args)
    {
        // helloWorld(args);
        // task1(args);
        // task2_blockedRing(args);
        // task2_unblockedRing(args);
    }

    public static void helloWorld(String[] args)
    {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        System.out.println("Hello, World! - from process <" + rank + "> of " + size);

        MPI.Finalize();
    }

    public static void task1(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int tag = 0;
        int[] message = new int[] { rank };

        if (rank % 2 == 0) {
            if (rank + 1 != size)
                MPI.COMM_WORLD.Send(message, 0, 1, MPI.INT, rank + 1, tag);
        } else {
            MPI.COMM_WORLD.Recv(message, 0, 1, MPI.INT, rank - 1, tag);
            System.out.printf("Process <" + rank + "> received message: " + Arrays.toString(message) + "\n");
        }

        MPI.Finalize();
    }

    public static void task2_blockedRing(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int tag = 0;

        int buf;
        int[] s = new int[1];

        if (rank == 0) {
            buf = rank;
            s[0] = buf;

            MPI.COMM_WORLD.Sendrecv(s, 0, 1, MPI.INT, rank + 1, tag,
                    s, 0, 1, MPI.INT, size - 1, tag);
        } else {
            MPI.COMM_WORLD.Recv(s, 0, 1, MPI.INT, rank - 1, tag);

            buf = rank;
            s[0] += buf;

            if (rank + 1 != size) {
                MPI.COMM_WORLD.Send(s, 0, 1, MPI.INT, rank + 1, tag);
            } else {
                MPI.COMM_WORLD.Send(s, 0, 1, MPI.INT, 0, tag);
            }
        }

        System.out.println("Rank <" + rank + ">: s = " + Integer.toString(s[0]));

        MPI.Finalize();
    }

    public static void task2_unblockedRing(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int tag = 0;

        int buf;
        int[] s = new int[1];

        if (rank == 0) {
            buf = rank;
            s[0] = buf;

            MPI.COMM_WORLD.Isend(s, 0, 1, MPI.INT, rank + 1, tag).Wait();
            MPI.COMM_WORLD.Irecv(s, 0, 1, MPI.INT, size - 1, tag).Wait();
        } else {
            MPI.COMM_WORLD.Irecv(s, 0, 1, MPI.INT, rank - 1, tag).Wait();

            buf = rank;
            s[0] += buf;

            if (rank + 1 != size) {
                MPI.COMM_WORLD.Isend(s, 0, 1, MPI.INT, rank + 1, tag).Wait();
            } else {
                MPI.COMM_WORLD.Isend(s, 0, 1, MPI.INT, 0, tag).Wait();
            }
        }

        System.out.println("Rank <" + rank + ">: s = " + Integer.toString(s[0]));

        MPI.Finalize();
    }
}