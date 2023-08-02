package reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class ReportsModel(
    private val reportsRepo: ReportsRepo,
) : ViewModel() {

    val args = MutableStateFlow<Args?>(null)

    val data = MutableStateFlow<Data?>(null)

    init {
        viewModelScope.launch {
            args.collect { args ->
                if (args == null) {
                    return@collect
                } else {
                    data.update {
                        val reports = reportsRepo.selectByAreaId(args.areaId)

                        withContext(Dispatchers.IO) {
                            Data(
                                verifiedPlaces = reports.mapNotNull {
                                    Pair(
                                        first = it.date.toString(),
                                        second = it.tags["up_to_date_elements"]?.jsonPrimitive?.longOrNull
                                            ?: return@mapNotNull null
                                    )
                                },
                                totalPlaces = reports.mapNotNull {
                                    Pair(
                                        first = it.date.toString(),
                                        second = it.tags["total_elements"]?.jsonPrimitive?.longOrNull
                                            ?: return@mapNotNull null
                                    )
                                },
                                verifiedPlacesFraction = reports.mapNotNull {
                                    val upToDateElements =
                                        it.tags["up_to_date_elements"]?.jsonPrimitive?.longOrNull
                                            ?: return@mapNotNull null
                                    val totalElements =
                                        it.tags["total_elements"]?.jsonPrimitive?.longOrNull
                                            ?: return@mapNotNull null

                                    Pair(
                                        first = it.date.toString(),
                                        second = upToDateElements.toFloat() / totalElements.toFloat() * 100f
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    data class Args(val areaId: String)

    data class Data(
        val verifiedPlaces: List<Pair<String, Long>>,
        val totalPlaces: List<Pair<String, Long>>,
        val verifiedPlacesFraction: List<Pair<String, Float>>,
    )
}