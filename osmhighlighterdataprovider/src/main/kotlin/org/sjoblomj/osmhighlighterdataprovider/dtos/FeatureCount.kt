package org.sjoblomj.osmhighlighterdataprovider.dtos

import org.sjoblomj.osmhighlighterdataprovider.db.maxNumberOfFeaturesReturned

data class FeatureCount(val numberOfFeaturesInBBox: Int) {
	@SuppressWarnings("unused") // Used in Json response
	val maxNumberOfFeaturesReturnedFromQuery = maxNumberOfFeaturesReturned
}
