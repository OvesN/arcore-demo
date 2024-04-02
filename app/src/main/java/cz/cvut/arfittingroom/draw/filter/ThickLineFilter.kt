import android.opengl.GLES20
import com.chillingvan.canvasgl.ICanvasGL
import com.chillingvan.canvasgl.shapeFilter.DrawShapeFilter
import cz.cvut.arfittingroom.model.Vector
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ThickLineFilter(points: List<Vector>, lineWidth: Float) : DrawShapeFilter {

    private val vertexShaderCode =
        "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = vPosition;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"
//
//    private var mPositionHandle: Int = 0
//    private var mColorHandle: Int = 0
//    private var vertexBuffer: FloatBuffer
//    private val COORDS_PER_VERTEX = 2
//    private var color = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
//    private var lineWidth: Float = 0f

    init {
//        // Generate vertices for thick line
//        val vertices = calculateBuffer(points, lineWidth)
//
//        // Initialize vertex byte buffer for shape coordinates
//        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
//        bb.order(ByteOrder.nativeOrder())
//        vertexBuffer = bb.asFloatBuffer()
//        vertexBuffer.put(vertices)
//        vertexBuffer.position(0)

    }




    override fun getVertexShader(): String {
        return vertexShaderCode
    }

    override fun getFragmentShader(): String {
        return fragmentShaderCode
    }

    override fun onPreDraw(program: Int, canvas: ICanvasGL?) {
        TODO("Not yet implemented")
    }


    override fun destroy() {

    }
}


