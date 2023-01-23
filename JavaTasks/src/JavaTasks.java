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
        //helloWorld(args);
        //task1(args);
        //task2_blockedRing(args);
        //task2_unblockedRing(args);
        //task3(args);
        //task4(args);
        task5_6(args);
        //task7(args);
        //task8(args);
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

    // последовательный +
    // блокирующий: стандартный +, буферизированный B, синхронизированный S, по готовности R
    // неблокирующий: стандартный +
    private static void task5_6(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        for (int N : new int[] {20}) {//, 100, 500}) {
            int[] a = new int[N];
            int[] b = new int[N];
            int[] c = new int[N];

            Random rnd = new Random();
            for (int i = 0; i < N; ++i) {
                a[i] = 1;
                b[i] = i;

                // a[i] = rnd.nextInt(9) + 1;
                // b[i] = rnd.nextInt(9) + 1;
            }

            MPI.COMM_WORLD.Barrier();

            // последовательынй
            if (rank == 0) {
                long startTime = System.currentTimeMillis();

                for (int i = 0; i < N; i++) {
                    c[i] = 0;
                    for (int j = 0; j < N; j++)
                        c[i] += a[i] * b[j];
                }

                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                System.out.println("N = " + N);
                System.out.println("serial mode: " + time + " ms");
                System.out.println("array a: " + Arrays.toString(a));
                System.out.println("array b: " + Arrays.toString(b));
                System.out.println("array c: " + Arrays.toString(c));
            }

            MPI.COMM_WORLD.Barrier();

            // стандартный блокирующий
            if (rank == 0) {
                long startTime = System.currentTimeMillis();

                int div = N / size;
                int mod = N % size;

                int extra_offset = (mod > 0 ? 1 : 0);
                int count = div + extra_offset;

                for (int i = 0; i < count; i++) {
                    c[i] = 0;
                    for (int j = 0; j < N; j++)
                        c[i] += a[i] * b[j];
                }

                mod--;

                for (int i = 1; i < size; i++) {
                    extra_offset += (mod > 0 ? 1 : 0);
                    int offset = i * div + extra_offset;
                    count = div + (mod > 0 ? 1 : 0);
                    mod--;

                    MPI.COMM_WORLD.Send(a, offset, count, MPI.INT, i, 0);
                    MPI.COMM_WORLD.Send(b, 0, N, MPI.INT, i, 1);

                    MPI.COMM_WORLD.Recv(c, offset, count, MPI.INT, i, 2);
                }

                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                System.out.println("standard block mode: " + time + " ms");
                //System.out.println("array a: " + Arrays.toString(a));
                //System.out.println("array b: " + Arrays.toString(b));
                //System.out.println("array c: " + Arrays.toString(c));
            } else {
                Status status = MPI.COMM_WORLD.Probe(0, 0);
                int count = status.Get_count(MPI.INT);

                a = new int[count];
                b = new int[N];

                MPI.COMM_WORLD.Recv(a, 0, count, MPI.INT, 0, 0);
                MPI.COMM_WORLD.Recv(b, 0, N, MPI.INT, 0, 1);

                c = new int[count];

                for (int i = 0; i < count; i++) {
                    c[i] = 0;
                    for (int j = 0; j < N; j++)
                        c[i] += a[i] * b[j];
                }

                MPI.COMM_WORLD.Send(c, 0, count, MPI.INT, 0, 2);
            }

            MPI.COMM_WORLD.Barrier();

            // синхронизированный блокирующий
            if (rank == 0) {
                long startTime = System.currentTimeMillis();

                int div = N / size;
                int mod = N % size;

                int extra_offset = (mod > 0 ? 1 : 0);
                int count = div + extra_offset;

                for (int i = 0; i < count; i++) {
                    c[i] = 0;
                    for (int j = 0; j < N; j++)
                        c[i] += a[i] * b[j];
                }

                mod--;

                for (int i = 1; i < size; i++) {
                    extra_offset += (mod > 0 ? 1 : 0);
                    int offset = i * div + extra_offset;
                    count = div + (mod > 0 ? 1 : 0);
                    mod--;

                    MPI.COMM_WORLD.Ssend(a, offset, count, MPI.INT, i, 0);
                    MPI.COMM_WORLD.Ssend(b, 0, N, MPI.INT, i, 1);

                    MPI.COMM_WORLD.Recv(c, offset, count, MPI.INT, i, 2);
                }

                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                System.out.println("synchronized block mode: " + time + " ms");
                //System.out.println("array a: " + Arrays.toString(a));
                //System.out.println("array b: " + Arrays.toString(b));
                //System.out.println("array c: " + Arrays.toString(c));
            } else {
                Status status = MPI.COMM_WORLD.Probe(0, 0);
                int count = status.Get_count(MPI.INT);

                a = new int[count];
                b = new int[N];

                MPI.COMM_WORLD.Recv(a, 0, count, MPI.INT, 0, 0);
                MPI.COMM_WORLD.Recv(b, 0, N, MPI.INT, 0, 1);

                c = new int[count];

                for (int i = 0; i < count; i++) {
                    c[i] = 0;
                    for (int j = 0; j < N; j++)
                        c[i] += a[i] * b[j];
                }

                MPI.COMM_WORLD.Ssend(c, 0, count, MPI.INT, 0, 2);
            }

            MPI.COMM_WORLD.Barrier();

            // стандартный неблокирующий
            if (rank == 0) {
                long startTime = System.currentTimeMillis();

                int div = N / size;
                int mod = N % size;

                int extra_offset = (mod > 0 ? 1 : 0);
                int count = div + extra_offset;

                for (int i = 0; i < count; i++) {
                    c[i] = 0;
                    for (int j = 0; j < N; j++)
                        c[i] += a[i] * b[j];
                }

                mod--;

                for (int i = 1; i < size; i++) {
                    extra_offset += (mod > 0 ? 1 : 0);
                    int offset = i * div + extra_offset;
                    count = div + (mod > 0 ? 1 : 0);
                    mod--;

                    MPI.COMM_WORLD.Isend(a, offset, count, MPI.INT, i, 0);
                    MPI.COMM_WORLD.Isend(b, 0, N, MPI.INT, i, 1);

                    MPI.COMM_WORLD.Irecv(c, offset, count, MPI.INT, i, 2).Wait();
                }

                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                System.out.println("standard unblock mode: " + time + " ms");
                //System.out.println("array a: " + Arrays.toString(a));
                //System.out.println("array b: " + Arrays.toString(b));
                //System.out.println("array c: " + Arrays.toString(c));
            } else {
                Status status = MPI.COMM_WORLD.Probe(0, 0);
                int count = status.Get_count(MPI.INT);

                a = new int[count];
                b = new int[N];

                MPI.COMM_WORLD.Irecv(a, 0, count, MPI.INT, 0, 0).Wait();
                MPI.COMM_WORLD.Irecv(b, 0, N, MPI.INT, 0, 1).Wait();

                c = new int[count];

                for (int i = 0; i < count; i++) {
                    c[i] = 0;
                    for (int j = 0; j < N; j++)
                        c[i] += a[i] * b[j];
                }

                MPI.COMM_WORLD.Isend(c, 0, count, MPI.INT, 0, 2);
            }

            MPI.COMM_WORLD.Barrier();

            a = new int[N];
            b = new int[N];
            c = new int[N];

            for (int i = 0; i < N; ++i) {
                a[i] = 1;
                b[i] = i;

                // a[i] = rnd.nextInt(9) + 1;
                // b[i] = rnd.nextInt(9) + 1;
            }

            // коллективные операции: scatterv
            long startTime = 0;

            if (rank == 0)
                startTime = System.currentTimeMillis();

            int div = N / size;
            int mod = N % size;

            int[] sendcount = new int[size];
            int[] displs = new int[size];

            sendcount[0] = div + (mod > 0 ? 1 : 0);
            displs[0] = 0;
            int extra_offset = (mod > 0 ? 1 : 0);
            mod--;

            for (int i = 1; i < size; i++) {
                extra_offset += (mod > 0 ? 1 : 0);
                sendcount[i] = div + (mod > 0 ? 1 : 0);
                displs[i] = i * div + extra_offset;
                mod--;
            }

            mod = N % size;

            int count = div + (rank < mod ? 1 : 0);
            int[] buf_a = new int[count];

            MPI.COMM_WORLD.Scatterv(
                    a, 0, sendcount, displs, MPI.INT,
                    buf_a, 0, count, MPI.INT, 0);

            if (rank == 0) {
                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                System.out.println("scatterv: " + time + " ms");
//                System.out.println("array a: " + Arrays.toString(a));
//                System.out.println("array b: " + Arrays.toString(b));
//                System.out.println("array c: " + Arrays.toString(c));
            }

            // коллективные операции: bcast
            int[] buf_b;
            if (rank == 0)
                buf_b = b.clone();
            else
                buf_b = new int[N];

            MPI.COMM_WORLD.Bcast(buf_b, 0, N, MPI.INT, 0);

            c = new int[count];
            for (int i = 0; i < count; i++) {
                c[i] = 0;
                for (int j = 0; j < N; j++)
                    c[i] += a[i] * b[j];
            }

            if (rank == 0) {
                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                System.out.println("bcast: " + time + " ms");
            }

            // коллективные операции: gatherv
            int[] buf_c = new int[N];
            int[] recvcount = sendcount.clone();

            MPI.COMM_WORLD.Gatherv(
                    c, 0, count, MPI.INT,
                    buf_c, 0, recvcount, displs, MPI.INT, 0);

            if (rank == 0) {
                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                System.out.println("gatherv: " + time + " ms");
                //System.out.println("array a: " + Arrays.toString(a));
                //System.out.println("array b: " + Arrays.toString(b));
                //System.out.println("array c: " + Arrays.toString(buf_c));
            }

            //System.out.println("array c: " + Arrays.toString(c));
        }
    }

    private static void task7(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int N = 5;
        int[][] graph = new int[N][N];
        int[] degree = new int[N];

        Random rnd = new Random();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                graph[i][j] = rnd.nextInt(2);
            }
        }

        MPI.COMM_WORLD.Barrier();

        if (rank == 0) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++)
                    continue;
                System.out.println("graph [" + i + "]: " + Arrays.toString(graph[i]));
            }
        }

        MPI.COMM_WORLD.Barrier();

        if (rank == 0) {
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < N; i++) {
                degree[i] = 0;
                for (int j = 0; j < N; j++) {
                    if (graph[i][j] == 1)
                        degree[i] += 1;
                }
            }

            int max = degree[0];
            for (int i = 1; i < N; i++)
                if (degree[i] > max)
                    max = degree[i];

            long endTime = System.currentTimeMillis();
            long time = endTime - startTime;

            System.out.println("max degree: " + max);
            System.out.println("degree: " + Arrays.toString(degree));
            System.out.println("serial mode: " + time + " ms");
        }

        MPI.COMM_WORLD.Barrier();

        if (rank == 0) {
            Arrays.fill(degree, 0);
            long startTime = System.currentTimeMillis();

            degree[0] = 0;
            for (int j = 0; j < N; j++)
                if (graph[0][j] == 1)
                    degree[0] += 1;

            for (int i = 1; i < N; i++) {
                MPI.COMM_WORLD.Isend(graph[i], 0, N, MPI.INT, i, 0);
                MPI.COMM_WORLD.Irecv(degree, i, 1, MPI.INT, i, 1).Wait();
            }

            int max = degree[0];
            for (int i = 1; i < N; i++)
                if (degree[i] > max)
                    max = degree[i];

            long endTime = System.currentTimeMillis();
            long time = endTime - startTime;

            System.out.println("max degree: " + max);
            System.out.println("degree: " + Arrays.toString(degree));
            System.out.println("standard unblock mode: " + time + " ms");
        }
        else {
            int[] graph_i = new int[N];
            MPI.COMM_WORLD.Irecv(graph_i, 0, N, MPI.INT, 0, 0).Wait();

            int[] degree_i = new int[1];
            degree[0] = 0;
            for (int j = 0; j < N; j++)
                if (graph_i[j] == 1)
                    degree_i[0] += 1;

            MPI.COMM_WORLD.Isend(degree_i, 0, 1, MPI.INT, 0, 1);
        }
    }

    private static void task8(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        for (int N : new int[] {2}) {//, 3, 3, 10, 100, 500}) {
            System.out.println("+" + rank);
            int[][] graph = new int[N][N];
            int[] degree = new int[N];

            Random rnd = new Random();
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    graph[i][j] = rnd.nextInt(2);
                }
            }

            MPI.COMM_WORLD.Barrier();

            if (rank == 0) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++)
                        continue;
                    System.out.println("graph [" + i + "]: " + Arrays.toString(graph[i]));
                }
            }

            MPI.COMM_WORLD.Barrier();

            if (rank == 0) {
                long startTime = System.currentTimeMillis();

                for (int i = 0; i < N; i++) {
                    degree[i] = 0;
                    for (int j = 0; j < N; j++) {
                        if (graph[i][j] == 1)
                            degree[i] += 1;
                    }
                }

                boolean f = true;
                for (int i = 0; i < N - 1; i++)
                    if (degree[i] != degree[i + 1]) {
                        f = false;
                        break;
                    }

                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                System.out.println("N = " + N);
                System.out.println("regular: " + (f ? " yes" : "no"));
                System.out.println("degree: " + Arrays.toString(degree));
                System.out.println("serial mode: " + time + " ms");
            }

            MPI.COMM_WORLD.Barrier();

            if (rank == 0) {
                Arrays.fill(degree, 0);
                long startTime = System.currentTimeMillis();

                degree[0] = 0;
                for (int j = 0; j < N; j++)
                    if (graph[0][j] == 1)
                        degree[0] += 1;

                for (int i = 1; i < N; i++) {
                    MPI.COMM_WORLD.Isend(graph[i], 0, N, MPI.INT, i, 0);
                    MPI.COMM_WORLD.Irecv(degree, i, 1, MPI.INT, i, 1).Wait();
                }

                boolean f = true;
                for (int i = 0; i < N - 1; i++)
                    if (degree[i] != degree[i + 1]) {
                        f = false;
                        break;
                    }

                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                System.out.println("regular: " + (f ? " yes" : "no"));
                System.out.println("degree: " + Arrays.toString(degree));
                System.out.println("standard unblock mode: " + time + " ms");
            } else {
                int[] graph_i = new int[N];
                MPI.COMM_WORLD.Irecv(graph_i, 0, N, MPI.INT, 0, 0).Wait();

                int[] degree_i = new int[1];
                degree[0] = 0;
                for (int j = 0; j < N; j++)
                    if (graph_i[j] == 1)
                        degree_i[0] += 1;

                MPI.COMM_WORLD.Isend(degree_i, 0, 1, MPI.INT, 0, 1);
            }

            MPI.COMM_WORLD.Barrier();
        }
    }
}
