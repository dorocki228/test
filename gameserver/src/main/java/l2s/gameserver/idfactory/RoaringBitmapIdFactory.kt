package l2s.gameserver.idfactory

import com.google.common.flogger.FluentLogger
import org.roaringbitmap.RoaringBitmap
import java.util.concurrent.atomic.AtomicInteger

object RoaringBitmapIdFactory : IdFactory() {

    private val logger = FluentLogger.forEnclosingClass()

    private val bitmap: RoaringBitmap
    private val freeIdCount: AtomicInteger
    private val nextFreeId: AtomicInteger

    init {
        try {
            bitmap = RoaringBitmap()

            freeIdCount = AtomicInteger(LAST_OID)

            (0..FIRST_OID).forEach { usedObjectId ->
                bitmap.add(usedObjectId)
                freeIdCount.decrementAndGet()
            }

            extractUsedObjectIdTable().forEach { usedObjectId ->
                if (usedObjectId < FIRST_OID) {
                    logger.atWarning().log("Object Id $usedObjectId in db is less than minimum Id of $FIRST_OID")
                    return@forEach
                }

                bitmap.add(usedObjectId)
                freeIdCount.decrementAndGet()
            }

            nextFreeId = AtomicInteger(bitmap.nextAbsentValue(FIRST_OID).toInt())
            initialized = true

            logger.atInfo().log("${freeIdCount.get()} id's available.")
        } catch (e: Exception) {
            initialized = false
            throw IllegalArgumentException("RoaringBitmapIdFactory could not be initialized correctly!", e)
        }
    }

    @Synchronized
    override fun releaseId(objectId: Int) {
        if (objectId >= FIRST_OID) {
            bitmap.remove(objectId)
            freeIdCount.incrementAndGet()
            super.releaseId(objectId)
        } else {
            logger.atWarning().log("release objectId $objectId failed (< $FIRST_OID)")
        }
    }

    @Synchronized
    override fun getNextId(): Int {
        val newId = nextFreeId.get()
        bitmap.add(newId)
        freeIdCount.decrementAndGet()

        var nextFree = bitmap.nextAbsentValue(newId).toInt()
        if (bitmap.contains(nextFree)) {
            nextFree = bitmap.nextAbsentValue(0).toInt()
        }
        check(!bitmap.contains(nextFree)) { "Ran out of valid Id's." }

        nextFreeId.set(nextFree)

        return newId
    }

    @Synchronized
    override fun size(): Int {
        return freeIdCount.get()
    }

}