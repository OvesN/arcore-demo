package com.cvut.arfittingroom.model
/**
 * @author Veronika Ovsyannikova
 */

const val MASK_TEXTURE_FILE_NAME = "mask_texture.png"
const val MASK_FRAME_FILE_NAME = "frame"
const val MASK_FRAMES_DIR_NAME = "mask_frames"

const val MAKEUP_SLOT = "makeup"
const val MASK_TEXTURE_SLOT = "mask"

const val TRANSPARENT_CODE = 0xFF_000_000
const val TOUCH_TO_MOVE_THRESHOLD = 20f
const val SPAN_SLOP = 7
const val PREVIEW_BITMAP_SIZE = 400
const val BITMAP_SIZE = 1024
const val NUM_OF_ELEMENTS_IN_ROW = 3
const val NUM_OF_ELEMENTS_IN_ROW_BIG_MENU = 4
const val MAX_LOOK_NAME_LENGTH = 50

// Firebase collections paths
const val MAKEUP_COLLECTION = "makeup"
const val MAKEUP_TYPES_COLLECTION = "makeup_types"
const val ACCESSORY_TYPES_COLLECTION = "accessory_types"
const val BRUSHES_COLLECTION = "brushes"
const val COLORS_COLLECTION = "colors"
const val MODELS_COLLECTION = "models"
const val LOOKS_COLLECTION = "looks"
const val PREVIEW_COLLECTION = "preview"
const val IMAGES_COLLECTION = "images"
const val GIFS_COLLECTION = "gifs"

// Firebase attributes
const val IS_PUBLIC_ATTRIBUTE = "is_public"
const val TYPE_ATTRIBUTE = "type"
const val REF_ATTRIBUTE = "ref"
const val DEFAULT_COLOR_ATTRIBUTE = "default_color"
const val PREVIEW_ATTRIBUTE = "preview_ref"
const val SLOT_ATTRIBUTE = "slot"
const val AUTHOR_ATTRIBUTE = "author"
const val CREATED_AT_ATTRIBUTE = "created_at"
