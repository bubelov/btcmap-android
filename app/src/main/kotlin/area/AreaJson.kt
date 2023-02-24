package area

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.time.ZonedDateTime

@Serializable
data class AreaJson(
    val id: String,
    val tags: JsonObject,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String,
)

fun AreaJson.valid(): Boolean {
    return tags.contains("name") && tags.contains("geo_json")
}

fun AreaJson.toArea(): Area {
    return Area(
        id = id,
        tags = tags,
        createdAt = ZonedDateTime.parse(created_at),
        updatedAt = ZonedDateTime.parse(updated_at),
        deletedAt = if (deleted_at.isNotEmpty()) ZonedDateTime.parse(deleted_at) else null,
    )
}