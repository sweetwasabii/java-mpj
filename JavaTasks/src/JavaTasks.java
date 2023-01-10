import mpi.MPI;
import mpi.Request;
import mpi.Status;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;


public class JavaTasks {
    public static void main(String[] args)
    {
        // helloWorld(args);
        //task1(args);
        //task2_blockedRing(args);
        //task2_unblockedRing(args);
        //task3(args);
        task4(args);
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

    public static void task3(String[] args) {
        MPI.Init(args);

        Random random = new Random();
        int rand = random.nextInt(10);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int TAG = 0;

        int[] buf;

        int first = size - 1;
        int second = first / 2;
        int count = (size - 3) / 2;

        if (rank == 0) {
            buf = new int[size - 3];

            Request[] r = new Request[2];
            r[0] = MPI.COMM_WORLD.Irecv(buf, 0, count, MPI.INT, first, TAG);
            r[1] = MPI.COMM_WORLD.Irecv(buf, count, count, MPI.INT, second, TAG);
            Request.Waitall(r);

            buf = Arrays.stream(buf).sorted().toArray();

            System.out.printf("received: " + Arrays.toString(buf) + "\n");
        } else if (rank == first) {
            buf = new int[count];

            Request[] r = new Request[count];
            for (int source = 1; source <= count; source++)
                r[source - 1] = MPI.COMM_WORLD.Irecv(buf, source - 1, 1, MPI.INT, source, TAG);
            Request.Waitall(r);

            buf = Arrays.stream(buf).sorted().toArray();

            MPI.COMM_WORLD.Isend(buf, 0, count, MPI.INT, 0, TAG);
        } else if (rank == second) {
            buf = new int[count];

            Request[] r = new Request[count];
            for (int source = second + 1; source <= second + count; source++)
                r[source - (second + 1)] = MPI.COMM_WORLD.Irecv(buf, source - (second + 1), 1, MPI.INT, source, TAG);
            Request.Waitall(r);

            buf = Arrays.stream(buf).sorted().toArray();

            MPI.COMM_WORLD.Isend(buf, 0, count, MPI.INT, 0, TAG);
        } else {
            int dest = rank >= 1 && rank <= count ? first : second;

            buf = new int[]{rand};
            System.out.printf("rand: " + rand + "\n");

            MPI.COMM_WORLD.Isend(buf, 0, 1, MPI.INT, dest, TAG);
        }
        MPI.Finalize();
    }

    private static void task4(String[] args) {
        MPI.Init(args);

        int[] data = new int[1];
        int[] buf = {1, 3, 5};
        int count, TAG = 0;
        Status status;
        data[0] = 2016;

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (rank == 0) {
            MPI.COMM_WORLD.Send(data, 0, 1, MPI.INT, 2, TAG);
        } else if (rank == 1) {
            MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.INT, 2, TAG);
        } else if (rank == 2) {
            status = MPI.COMM_WORLD.Probe(0, TAG);
            count = status.Get_count(MPI.INT);
            int[] back_buf = new int[count];

            MPI.COMM_WORLD.Recv(back_buf, 0, count, MPI.INT, 0, TAG);
            System.out.println("Rank = 0");

            for (int i = 0; i < count; i++)
                System.out.println(back_buf[i]);

            status = MPI.COMM_WORLD.Probe(1, TAG);
            count = status.Get_count(MPI.INT);
            back_buf = new int[count];

            MPI.COMM_WORLD.Recv(back_buf, 0, count, MPI.INT, 1, TAG);
            System.out.println("Rank = 1");

            for (int i = 0; i < count; i++)
                System.out.println(back_buf[i] + " ");
        }

        MPI.Finalize();
    }
}
