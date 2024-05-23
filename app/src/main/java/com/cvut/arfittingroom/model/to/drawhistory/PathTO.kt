package com.cvut.arfittingroom.model.to.drawhistory

/**
 * Path transfer object
 * Used to serialize the path
 *
 * @property actions Each action describes a step in the path
 *
 * @author Veronika Ovsyannikova
 */
data class PathTO(var actions: List<PathActionTO> = emptyList())
