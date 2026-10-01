from alembic import op
import sqlalchemy as sa
revision='0001_initial'; down_revision=None; branch_labels=None; depends_on=None
def upgrade():
 op.create_table('users',sa.Column('id',sa.String(36),primary_key=True),sa.Column('email',sa.String(320),nullable=False),sa.Column('password_hash',sa.Text(),nullable=False),sa.Column('date_of_birth',sa.String(10),nullable=False),sa.Column('terms_version',sa.String(64),nullable=False),sa.Column('privacy_version',sa.String(64),nullable=False),sa.Column('created_at',sa.DateTime(),nullable=False))
 op.create_index('ix_users_email','users',['email'],unique=True)
def downgrade():
 op.drop_index('ix_users_email',table_name='users'); op.drop_table('users')
