package io.mockative.kontinuity

/**
 * Controls how a Kontinuity Wrapper is generated for the annotated class or interface.
 */
enum class KontinuityGeneration {
    /**
     * Disables generation of a Kontinuity Wrapper.
     */
    NONE,

    /**
     * Enables generation of a Kontinuity Wrapper, containing a member for each member of the
     * annotated type, which hasn't otherwise explicitly disabled generation using
     * [KontinuityMember.generate].
     */
    OPT_OUT,

    /**
     * Enables generation of a Kontinuity Wrapper, containing a member for each member of the
     * annotated type, which has also explicitly enabled generation using [KontinuityMember].
     */
    OPT_IN,
}
