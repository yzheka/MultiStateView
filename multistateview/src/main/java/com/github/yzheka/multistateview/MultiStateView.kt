package com.github.yzheka.multistateview

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.constraintlayout.widget.ConstraintLayout

class MultiStateView @JvmOverloads constructor(context: Context,attrs:AttributeSet?=null,defStyle:Int=0):ConstraintLayout(context, attrs,defStyle) {
    private var mInAnimation=AnimationUtils.loadAnimation(context,android.R.anim.fade_in)
    private var mOutAnimation=AnimationUtils.loadAnimation(context,android.R.anim.fade_out)
    /**
     * enables or disables animation when state changes
     */
    var areAnimationsEnabled=false
    private var inAnimDuration=mInAnimation.duration
    private var outAnimDuration=mOutAnimation.duration
    private var inAnimDurationOverride=0L
    private var outAnimDurationOverride=0L
    private var mState=ViewState.CONTENT
    private val mListeners=LinkedHashSet<OnViewStateChangeListener>()

    /**
     * Get or set state for displaying
     */
    var state get() = mState
        set(value) {
            if(mState==value)return
            mState=value
            switchToState(value,areAnimationsEnabled)
        }

    /**
     * Sets state to CONTENT
     */
    fun showContent(){
        state=ViewState.CONTENT
    }

    /**
     * Sets state to EMPTY
     */
    fun showEmpty(){
        state=ViewState.EMPTY
    }

    /**
     * Sets state to ERROR
     */
    fun showError(){
        state=ViewState.ERROR
    }

    /**
     * Sets state to LOADING
     */
    fun showLoading(){
        state=ViewState.LOADING
    }

    /**
     * Sets state to UNDEFINED
     */
    fun showUndefined(){
        state=ViewState.UNDEFINED
    }

    /**
     * Adds listener for observing state changes
     * @param listener listener for observing state changes
     */
    fun addOnViewStateChangeListener(listener:OnViewStateChangeListener){
        mListeners.add(listener)
    }

    /**
     * Removes listener for observing state changes
     * @param listener listener for observing state changes
     */
    fun removeOnViewStateChangeListener(listener: OnViewStateChangeListener){
        mListeners.remove(listener)
    }

    init {
        isSaveEnabled=true
        if(attrs!=null){
            val array=context.obtainStyledAttributes(attrs,R.styleable.MultiStateView)
            areAnimationsEnabled=array.getBoolean(R.styleable.MultiStateView_animationsEnabled,areAnimationsEnabled)
            setInAnimation(array.getResourceId(R.styleable.MultiStateView_inAnimation,0))
            setOutAnimation(array.getResourceId(R.styleable.MultiStateView_outAnimation,0))
            setInAnimationDuration(array.getInteger(R.styleable.MultiStateView_inAnimationDuration,0).toLong())
            setOutAnimationDuration(array.getInteger(R.styleable.MultiStateView_outAnimationDuration,0).toLong())
            val stateIndex=array.getInt(R.styleable.MultiStateView_initialState,0)
            mState=ViewState.values()[stateIndex]
            switchToState(mState,false)
            array.recycle()
        }
    }

    private fun switchToState(viewState: ViewState,runAnimation:Boolean){
        (0 until childCount).mapNotNull { getChildAt(it) }.forEach {
            val state = (it.layoutParams as? LayoutParams?)?.state
            if (state != null) {
                if (state == viewState) it.show(runAnimation)
                else it.hide(runAnimation)
            }
        }
        mListeners.forEach { it.onViewStateChanged(this,viewState) }
    }

    private fun View.hide(animate: Boolean){
        if(visibility!=View.VISIBLE||animation==mOutAnimation)return
        val params=layoutParams as? LayoutParams?: generateDefaultLayoutParams()
        val visibility=when(params.hideStrategy){
            HideStrategy.MAKE_INVISIBLE->View.INVISIBLE
            HideStrategy.MAKE_GONE->View.GONE
        }
        setVisibility(visibility)
        if(animate)startAnimation(mOutAnimation)
    }

    private fun View.show(animate:Boolean){
        if(visibility==View.VISIBLE||animation==mInAnimation)return
        visibility=View.VISIBLE
        if(animate)startAnimation(mInAnimation)
    }

    /**
     * Sets animation to use when view appears
     * @param animation animation to use. if null resets to default android.R.anim.fade_in
     */
    fun setInAnimation(animation: Animation?){
        mInAnimation=animation?:AnimationUtils.loadAnimation(context,android.R.anim.fade_in)
        inAnimDuration=mInAnimation.duration
        setInAnimationDuration(inAnimDurationOverride)
    }

    /**
     * Sets animation to use when view disappears
     * @param animation animation to use. if null resets to default android.R.anim.fade_out
     */
    fun setOutAnimation(animation: Animation?){
        mOutAnimation=animation?:AnimationUtils.loadAnimation(context,android.R.anim.fade_out)
        outAnimDuration=mInAnimation.duration
        setOutAnimationDuration(outAnimDurationOverride)
    }

    /**
     * Overrides in animation duration
     * @param duration duration in ms. if less than 0 resets to default animation duration
     */
    fun setInAnimationDuration(duration:Long){
        inAnimDurationOverride=duration
        mInAnimation.duration=if(duration>0)duration else inAnimDuration
    }

    /**
     * Overrides out animation duration
     * @param duration duration in ms. if less than 0 resets to default animation duration
     */
    fun setOutAnimationDuration(duration:Long){
        outAnimDurationOverride=duration
        mOutAnimation.duration=if(duration>0)duration else outAnimDuration
    }

    /**
     * Sets animation to use when view appears
     * @param animation animation to use. if 0 resets to default android.R.anim.fade_in
     */
    fun setInAnimation(@AnimRes animation:Int){
        if(animation==0)setInAnimation(null)
        else setInAnimation(AnimationUtils.loadAnimation(context,animation))
    }

    /**
     * Sets animation to use when view disappears
     * @param animation animation to use. if 0 resets to default android.R.anim.fade_out
     */
    fun setOutAnimation(@AnimRes animation:Int){
        if(animation==0)setOutAnimation(null)
        else setOutAnimation(AnimationUtils.loadAnimation(context,animation))
    }

    override fun onViewAdded(view: View?) {
        super.onViewAdded(view)
        val params=view?.layoutParams as? LayoutParams? ?: generateDefaultLayoutParams()
        if(params.state?:return==mState)view?.show(false)
        else view?.hide(false)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean = p is LayoutParams

    override fun generateDefaultLayoutParams():LayoutParams = LayoutParams(context,null)

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams = LayoutParams(context,attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams = LayoutParams(p)

    override fun onSaveInstanceState(): Parcelable? {
        val state=SavedState(super.onSaveInstanceState())
        state.inAnimDuration=inAnimDuration
        state.outAnimDuration=outAnimDuration
        state.inAnimDurationOverride=inAnimDurationOverride
        state.outAnimDurationOverride=outAnimDurationOverride
        state.mState=mState
        state.areAnimationsEnabled=areAnimationsEnabled
        state.inAnimation=mInAnimation
        state.outAnimation=mOutAnimation
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedSate=state as? SavedState?
        super.onRestoreInstanceState(savedSate?.superState)
        inAnimDuration=savedSate?.inAnimDuration?:inAnimDuration
        outAnimDuration=savedSate?.outAnimDuration?:outAnimDuration
        inAnimDurationOverride=savedSate?.inAnimDurationOverride?:inAnimDurationOverride
        outAnimDurationOverride=savedSate?.outAnimDurationOverride?:outAnimDurationOverride
        mState=savedSate?.mState?:mState
        areAnimationsEnabled=savedSate?.areAnimationsEnabled?:areAnimationsEnabled
        mInAnimation=savedSate?.inAnimation?:mInAnimation
        mOutAnimation=savedSate?.outAnimation?:mOutAnimation
        switchToState(mState,false)
    }

    class SavedState:BaseSavedState{
        var inAnimDuration=0L
        var outAnimDuration=0L
        var inAnimDurationOverride=0L
        var outAnimDurationOverride=0L
        var mState=ViewState.CONTENT
        var areAnimationsEnabled=false
        var inAnimation:Animation?=null
        var outAnimation:Animation?=null
        constructor(source: Parcel?) : super(source){
            inAnimDuration=source?.readLong()?:inAnimDuration
            outAnimDuration=source?.readLong()?:outAnimDuration
            inAnimDurationOverride=source?.readLong()?:inAnimDurationOverride
            outAnimDurationOverride=source?.readLong()?:outAnimDurationOverride
            mState=runCatching { ViewState.valueOf(source?.readString()?:"") }.getOrDefault(mState)
            areAnimationsEnabled=source?.readBoolean()?:areAnimationsEnabled
            inAnimation=source?.readSerializable() as? Animation?
            outAnimation=source?.readSerializable() as? Animation?
        }
        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeLong(inAnimDuration)
            out?.writeLong(outAnimDuration)
            out?.writeLong(inAnimDurationOverride)
            out?.writeLong(outAnimDurationOverride)
            out?.writeString(mState.name)
            out?.writeBoolean(areAnimationsEnabled)
            out?.writeSerializable(inAnimDuration)
            out?.writeSerializable(outAnimDuration)
        }

        companion object CREATOR:Parcelable.Creator<SavedState>{
            override fun createFromParcel(source: Parcel?): SavedState = SavedState(source)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    class LayoutParams:ConstraintLayout.LayoutParams{
        /**
         * State to be used for this view. if null view visibility  will not change on state changes
         */
        var state:ViewState?=null
        /**
         * Strategy to use for view hiding
         */
        var hideStrategy = HideStrategy.MAKE_INVISIBLE
        constructor(source: ConstraintLayout.LayoutParams?) : super(source)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
            if(attrs==null)return
            val array=context.obtainStyledAttributes(attrs, R.styleable.MultiStateView_Layout)
            var index=array.getInt(R.styleable.MultiStateView_Layout_layout_state,-1)
            state=ViewState.values().getOrNull(index)
            index=array.getInt(R.styleable.MultiStateView_Layout_layout_hideStrategy,-1)
            hideStrategy=HideStrategy.values().getOrNull(index)?:hideStrategy
            array.recycle()
        }
        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.LayoutParams?) : super(source){
            if(source is LayoutParams){
                state=source.state
                hideStrategy=source.hideStrategy
            }
        }
    }

    /**
     * THe way how to hide view
     */
    enum class HideStrategy{
        /**
         * Makes view invisible when it's state not matches with {@link MultiStateView#state state}
         */
        MAKE_INVISIBLE,
        /**
         * Makes view gone when it's state not matches with {@link MultiStateView#state state}
         */
        MAKE_GONE
    }

    /**
     * State for displaying
     */
    enum class ViewState{
        /**
         * State for displaying content view
         */
        CONTENT,
        /**
         * State for displaying empty view
         */
        EMPTY,
        /**
         * State for displaying error view
         */
        ERROR,
        /**
         * State for displaying loading view
         */
        LOADING,
        /**
         * State for displaying undefined view
         */
        UNDEFINED
    }

    /**
     * Listener for observing state changes
     */
    interface OnViewStateChangeListener{
        /**
         * Called when {@link MultiStateView#state state} changes
         * @param view owner
         * @param viewState new state
         */
        fun onViewStateChanged(view:MultiStateView,viewState: ViewState)
    }

    companion object{
        private fun Animation.onAnimationEnd(listener:()->Unit):Animation = apply{
            setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationRepeat(animation: Animation?) = Unit
                override fun onAnimationStart(animation: Animation?) = Unit
                override fun onAnimationEnd(animation: Animation?) = listener()
            })
        }
    }
}