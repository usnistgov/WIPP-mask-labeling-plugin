/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.masklabelingplugin.utils;

/**
 * Created by mmajursk on 4/8/2014. Taken from
 * https://vm-070.nist.gov/gitweb/?p=Image-Analysis;a=blob;f=Utilities/Java/Plugins/Empirical_Gradient_Threshold/src/gov/nist/isg/egt/ConnectedComponents.java;hb=HEAD
 */
public class ConnectedComponents {

    public static int bwlabel4(int[] pixeldata, int n, int m) {

        int label = 1;
        for (int i = 0; i < m; i++) {
            // first element in the row
            int k = i * n;
            if (pixeldata[k] > 0) {
                pixeldata[k] = label++;
            }

            for (int j = (i * n + 1); j < ((i + 1) * n); j++) {
                if (pixeldata[j] > 0) {
                    if (pixeldata[j - 1] > 0) {
                        pixeldata[j] = pixeldata[j - 1];
                    } else {
                        pixeldata[j] = label++;
                    }
                }
            }
        }

        // build equivalency tables using the run length encoded values
        UnionFind uf = new UnionFind(m);
        for (int i = 1; i < m; i++) {
            for (int j = (i * n); j < ((i + 1) * n); j++) {
                if (pixeldata[j] > 0 && pixeldata[j - n] > 0) {
                    uf.union(pixeldata[j - n], pixeldata[j]);
                }
            }
        }

        // relabel the image to match the root labels
        int[] labels = new int[label];
        for (int i = 0; i < label; i++) {
            labels[uf.root(i)] = 1;
        }
        int newNb;
        if (labels[0] == 1) {
            newNb = 0;
        } else {
            newNb = 1;
        }
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] > 0) {
                labels[i] = newNb++;
            }
        }

        // second pass to relabel the image
        for (int i = 0; i < pixeldata.length; i++) {
            if (pixeldata[i] > 0) {
                pixeldata[i] = labels[uf.root(pixeldata[i])];
            }
        }

        return (newNb - 1);
    }

    public static int bwlabel8(int[] pixeldata, int n, int m) {

        int label = 1;
        for (int i = 0; i < m; i++) {
            // first element in the row
            int k = i * n;
            if (pixeldata[k] > 0) {
                pixeldata[k] = label++;
            }

            for (int j = (i * n + 1); j < ((i + 1) * n); j++) {
                if (pixeldata[j] > 0) {
                    if (pixeldata[j - 1] > 0) {
                        pixeldata[j] = pixeldata[j - 1];
                    } else {
                        pixeldata[j] = label++;
                    }
                }
            }
        }

        // build equivalency tables using the run length encoded values
        UnionFind uf = new UnionFind(m);
        for (int i = 1; i < m; i++) {
            // check first element in the row
            int k = i * n;
            if (pixeldata[k] > 0) {
                if (pixeldata[k - n] > 0) {
                    uf.union(pixeldata[k - n], pixeldata[k]);
                } else {
                    if (pixeldata[k - n + 1] > 0) {
                        uf.union(pixeldata[k - n + 1], pixeldata[k]);
                    }
                }
            }

            for (int j = (i * n + 1); j < ((i + 1) * n - 1); j++) {
                if (pixeldata[j] > 0) {
                    if (pixeldata[j - n - 1] > 0) {
                        uf.union(pixeldata[j - n - 1], pixeldata[j]);
                        if (pixeldata[j - n + 1] > 0) {
                            uf.union(pixeldata[j - n + 1], pixeldata[j]);
                        }
                    } else if (pixeldata[j - n] > 0) {
                        uf.union(pixeldata[j - n], pixeldata[j]);
                    } else if (pixeldata[j - n + 1] > 0) {
                        uf.union(pixeldata[j - n + 1], pixeldata[j]);
                    }
                }
            }

            // check last element in the row
            k = ((i + 1) * n - 1);
            if (pixeldata[k] > 0) {
                if (pixeldata[k - n - 1] > 0) {
                    uf.union(pixeldata[k - n - 1], pixeldata[k]);
                } else {
                    if (pixeldata[k - n] > 0) {
                        uf.union(pixeldata[k - n], pixeldata[k]);
                    }
                }
            }
        }

        // relabel the image to match the root labels
        int[] labels = new int[label];
        for (int i = 0; i < label; i++) {
            labels[uf.root(i)] = 1;
        }
        int newNb;
        if (labels[0] == 1) {
            newNb = 0;
        } else {
            newNb = 1;
        }
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] > 0) {
                labels[i] = newNb++;
            }
        }

        // second pass to relabel the image
        for (int i = 0; i < pixeldata.length; i++) {
            if (pixeldata[i] > 0) {
                pixeldata[i] = labels[uf.root(pixeldata[i])];
            }
        }

        return (newNb - 1);
    }

    public static void erodeDisk1(int[] pixeldata, int n, int m) {

    // pixeldata is binary coming into the function
        // make pixeldata binary, [0,1]
//        for(int k = 0; k < pixeldata.length; k++) {
//            if(pixeldata[k] > 0) pixeldata[k] = 1;
//        }
        // handle the first element
        if (pixeldata[0] == 1) {
            if (pixeldata[1] == 0 || pixeldata[n] == 0) {
                pixeldata[0] = 2;
            }
        }

        // handle the first row
        int endindx = n - 1;
        for (int k = 1; k < endindx; k++) {
            if (pixeldata[k] > 0) {
                if (pixeldata[k - 1] == 0 || pixeldata[k + 1] == 0 || pixeldata[k + n] == 0) {
                    pixeldata[k] = 2;
                }
            }
        }
        // last element in the first row
        if (pixeldata[endindx] > 0) {
            if (pixeldata[endindx - 1] == 0 || pixeldata[endindx + n] == 0) {
                pixeldata[endindx] = 2;
            }
        }

        // loop over rows 1 through (m-2)
        int k = n;
        for (int i = 1; i < (m - 1); i++) {
            k = i * n;
            // check the first element
            if (pixeldata[k] > 0) {
                if (pixeldata[k + 1] == 0 || pixeldata[k - n] == 0 || pixeldata[k + n] == 0) {
                    pixeldata[k] = 2;
                }
            }

            // check the row except the last element
            endindx = k + n - 1;
            k++;
            for (; k < endindx; k++) {
                if (pixeldata[k] > 0) {
                    if (pixeldata[k - 1] == 0 || pixeldata[k + 1] == 0 || pixeldata[k - n] == 0
                            || pixeldata[k + n] == 0) {
                        pixeldata[k] = 2;
                    }
                }
            }

            // check the last element in the row
            if (pixeldata[k] > 0) {
                if (pixeldata[k - 1] == 0 || pixeldata[k - n] == 0 || pixeldata[k + n] == 0) {
                    pixeldata[k] = 2;
                }
            }
        }

        // handle the last row
        endindx = k + n - 1;
        k++;
        for (; k < endindx; k++) {
            if (pixeldata[k] > 0) {
                if (pixeldata[k - 1] == 0 || pixeldata[k + 1] == 0 || pixeldata[k - n] == 0) {
                    pixeldata[k] = 2;
                }
            }
        }
        // last element in the last row
        if (pixeldata[k] > 0) {
            if (pixeldata[k - 1] == 0 || pixeldata[k - n] == 0) {
                pixeldata[k] = 2;
            }
        }

        // remove the 2s
        for (k = 0; k < pixeldata.length; k++) {
            if (pixeldata[k] == 2) {
                pixeldata[k] = 0;
            }
        }

    // make pixeldata binary, [0,1]
//        for (k = 0; k < pixeldata.length; k++) {
//            if (pixeldata[k] > 0) pixeldata[k] = 1;
//        }
    }

    public static class UnionFind {

        private int[] id;    // id[i] = parent of i
        private int[] sz;

        // Create an empty union find data structure with N isolated sets.
        public UnionFind(int N) {
            id = new int[N];
            sz = new int[N];
            for (int i = 0; i < N; i++) {
                id[i] = i;
                sz[i] = 1;
            }
        }

        public int root(int i) {
            // adjust the size of id, sz based on the search location i
            if (i >= id.length) {
                int[] id2 = id;
                int[] sz2 = sz;
                id = new int[id2.length * 2];
                sz = new int[sz2.length * 2];
                System.arraycopy(id2, 0, id, 0, id2.length);
                System.arraycopy(sz2, 0, sz, 0, sz2.length);
                for (int k = id2.length; k < id.length; k++) {
                    id[k] = k;
                    sz[k] = 1;
                }
            }

            while (i != id[i]) {
                id[i] = id[id[i]];
                i = id[i];
            }
            return i;
        }

        public boolean find(int p, int q) {
            return root(p) == root(q);
        }

        public void union(int p, int q) {
            int i = root(p);
            int j = root(q);
            if (i == j) {
                return;
            }
            if (sz[i] < sz[j]) {
                id[i] = j;
                sz[j] += sz[i];
            } else {
                id[j] = i;
                sz[i] += sz[j];
            }
        }

    }

}

