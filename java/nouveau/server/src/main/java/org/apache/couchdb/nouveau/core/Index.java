//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.couchdb.nouveau.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.couchdb.nouveau.api.DocumentDeleteRequest;
import org.apache.couchdb.nouveau.api.DocumentUpdateRequest;
import org.apache.couchdb.nouveau.api.IndexInfo;
import org.apache.couchdb.nouveau.api.SearchRequest;
import org.apache.couchdb.nouveau.api.SearchResults;

public abstract class Index implements Closeable {

    /*
     * The close lock is to ensure there are no readers/searchers when
     * we want to close the index.
     */
    private ReentrantReadWriteLock closeLock = new ReentrantReadWriteLock();

    /*
     * The update lock ensures serial updates to the index.
     */
    private ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();

    private long updateSeq;

    private boolean deleteOnClose = false;

    private boolean closed = false;

    protected Index(final long updateSeq) {
        this.updateSeq = updateSeq;
    }

    public final IndexInfo info() throws IOException {
        final long updateSeq = getUpdateSeq();
        closeLock.readLock().lock();
        try {
            final int numDocs = doNumDocs();
            return new IndexInfo(updateSeq, numDocs);
        } finally {
            closeLock.readLock().unlock();
        }
    }

    protected abstract int doNumDocs() throws IOException;

    public final void update(final String docId, final DocumentUpdateRequest request) throws IOException {
        updateLock.writeLock().lock();
        try {
            assertUpdateSeqIsLower(request.getSeq());
            closeLock.readLock().lock();
            try {
                doUpdate(docId, request);
            } finally {
                closeLock.readLock().unlock();
            }
            incrementUpdateSeq(request.getSeq());
        } finally {
            updateLock.writeLock().unlock();
        }
    }

    protected abstract void doUpdate(final String docId, final DocumentUpdateRequest request) throws IOException;

    public final void delete(final String docId, final DocumentDeleteRequest request) throws IOException {
        updateLock.writeLock().lock();
        try {
            assertUpdateSeqIsLower(request.getSeq());
            closeLock.readLock().lock();
            try {
                doDelete(docId, request);
            } finally {
                closeLock.readLock().unlock();
            }
            incrementUpdateSeq(request.getSeq());
        } finally {
            updateLock.writeLock().unlock();
        }
    }

    protected abstract void doDelete(final String docId, final DocumentDeleteRequest request) throws IOException;

    public final SearchResults search(final SearchRequest request) throws IOException, QueryParserException {
        closeLock.readLock().lock();
        try {
            return doSearch(request);
        } finally {
            closeLock.readLock().unlock();
        }
    }

    protected abstract SearchResults doSearch(final SearchRequest request) throws IOException, QueryParserException;

    public final boolean commit() throws IOException {
        final long updateSeq = getUpdateSeq();
        closeLock.readLock().lock();
        try {
            return doCommit(updateSeq);
        } finally {
            closeLock.readLock().unlock();
        }
    }

    protected abstract boolean doCommit(final long updateSeq) throws IOException;

    public final void close() throws IOException {
        closeLock.writeLock().lock();
        try {
            doClose(deleteOnClose);
            closed = true;
        } finally {
            closeLock.writeLock().unlock();
        }
    }

    protected abstract void doClose(final boolean deleteOnClose) throws IOException;

    public final void lock() {
        closeLock.readLock().lock();
    }

    public final void unlock() {
        closeLock.readLock().unlock();
    }

    public final boolean isClosed() {
        return closed;
    }

    public final void setDeleteOnClose(final boolean deleteOnClose) {
        closeLock.writeLock().lock();
        try {
            this.deleteOnClose = true;
        } finally {
            closeLock.writeLock().unlock();
        }
    }

    protected final void assertUpdateSeqIsLower(final long updateSeq) throws UpdatesOutOfOrderException {
        assert updateLock.isWriteLockedByCurrentThread();
        if (!(updateSeq > this.updateSeq)) {
            throw new UpdatesOutOfOrderException();
        }
    }

    protected final void incrementUpdateSeq(final long updateSeq) throws IOException {
        assert updateLock.isWriteLockedByCurrentThread();
        assertUpdateSeqIsLower(updateSeq);
        this.updateSeq = updateSeq;
    }

    private long getUpdateSeq() {
        updateLock.readLock().lock();
        try {
            return this.updateSeq;
        } finally {
            updateLock.readLock().unlock();
        }
    }

}