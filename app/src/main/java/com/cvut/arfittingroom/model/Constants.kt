package com.cvut.arfittingroom.model

const val MASK_TEXTURE_FILE_NAME = "mask_texture.png"
const val MASK_FRAME_FILE_NAME = "frame"
const val MASK_FRAMES_DIR_NAME = "mask_frames"

const val MAKEUP_SLOT = "makeup"
const val MASK_TEXTURE_SLOT = "mask"

const val TRANSPARENT_CODE = 0xFF_000_000
const val TOUCH_TO_MOVE_THRESHOLD = 20f
const val SPAN_SLOP = 7

const val NUM_OF_ELEMENTS_IN_ROW = 3

const val MAX_LOOK_NAME_LENGTH = 50

// Firebase collections paths
const val USERS_COLLECTION = "users"
const val MAKEUP_COLLECTION = "makeup"
const val MAKEUP_TYPES_COLLECTION = "makeup_types"
const val ACCESSORY_TYPES_COLLECTION = "accessory_types"
const val COLORS_COLLECTION = "colors"
const val MODELS_COLLECTION = "models"
const val LOOKS_COLLECTION = "looks"
const val PREVIEW_COLLECTION = "preview"

// Firebase attributes
const val LOOK_ID_ATTRIBUTE = "look_id"
const val IS_PUBLIC_ATTRIBUTE = "is_public"
const val TYPE_ATTRIBUTE = "type"
const val REF_ATTRIBUTE = "ref"
const val DEFAULT_COLOR_ATTRIBUTE = "default_color"
const val PREVIEW_IMAGE_ATTRIBUTE = "preview_image"
const val SLOT_ATTRIBUTE = "slot"
const val AUTHOR_ATTRIBUTE = "author"
const val IS_ANIMATED_ATTRIBUTE = "is_animated"
const val APPLIED_MAKEUP_ATTRIBUTE = "applied_makeup"
const val APPLIED_MODELS_ATTRIBUTE = "applied_models"
const val NAME_ATTRIBUTE = "name"
const val HISTORY_ATTRIBUTE = "history"